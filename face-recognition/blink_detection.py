"""Per-request blink and head-turn challenge using MediaPipe landmarks.

Recognize a natural open -> blink -> open sequence from a short high-frequency
capture window. This is active liveness, not a video replay detector.
"""
from pathlib import Path
import threading
import cv2

_lock = threading.Lock()
_landmarker = None


def blink_sequence(scores):
    phase = 0
    opened_frames = 0
    closed_frames = 0
    reopened_frames = 0
    for left, right in scores:
        opened = left < 0.35 and right < 0.35
        closed = left > 0.48 and right > 0.48
        strongly_closed = left > 0.82 and right > 0.82
        if phase == 0:
            opened_frames = opened_frames + 1 if opened else 0
            if opened_frames >= 2:
                phase = 1
        elif phase == 1:
            if strongly_closed:
                phase = 2
                reopened_frames = 0
            elif closed:
                closed_frames += 1
                if closed_frames >= 2:
                    phase = 2
                    reopened_frames = 0
            elif opened:
                closed_frames = 0
        else:
            reopened_frames = reopened_frames + 1 if opened else 0
            if reopened_frames >= 2:
                return True
    return False


def head_turn_sequence(yaw_scores, threshold=0.10):
    """Accept a clear turn to both sides relative to the initial front pose."""
    if len(yaw_scores) < 8:
        return False
    baseline_values = yaw_scores[:3]
    baseline = sorted(baseline_values)[len(baseline_values) // 2]
    deltas = [value - baseline for value in yaw_scores]
    positive = [index for index, value in enumerate(deltas) if value >= threshold]
    negative = [index for index, value in enumerate(deltas) if value <= -threshold]
    if not positive or not negative:
        return False
    return positive[0] < negative[-1] or negative[0] < positive[-1]


def analyze_frames(images):
    global _landmarker
    import mediapipe as mp
    with _lock:
        if _landmarker is None:
            options = mp.tasks.vision.FaceLandmarkerOptions(
                # Native Windows model loaders may not support non-ASCII paths.
                base_options=mp.tasks.BaseOptions(model_asset_buffer=(Path(__file__).parent / 'models' / 'face_landmarker.task').read_bytes()),
                running_mode=mp.tasks.vision.RunningMode.IMAGE,
                num_faces=2,
                output_face_blendshapes=True,
                output_facial_transformation_matrixes=True,
            )
            _landmarker = mp.tasks.vision.FaceLandmarker.create_from_options(options)
        scores = []
        yaw_scores = []
        for image in images:
            rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            result = _landmarker.detect(mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb))
            if len(result.face_blendshapes) != 1:
                raise ValueError('请保持单人入镜，完成动作时不要离开画面')
            values = {value.category_name: value.score for value in result.face_blendshapes[0]}
            if 'eyeBlinkLeft' not in values or 'eyeBlinkRight' not in values:
                raise ValueError('眼部关键点检测不可用')
            scores.append((values['eyeBlinkLeft'], values['eyeBlinkRight']))
            landmarks = result.face_landmarks[0]
            eye_distance = abs(landmarks[263].x - landmarks[33].x)
            if eye_distance < 0.05:
                raise ValueError('面部角度过大，请缓慢转头并保持在取景框内')
            eye_midpoint = (landmarks[33].x + landmarks[263].x) / 2
            yaw_scores.append((landmarks[1].x - eye_midpoint) / eye_distance)
        return scores, yaw_scores


def validate_liveness_step(images, step):
    """Validate one challenge before the client is allowed to continue."""
    scores, yaw_scores = analyze_frames(images)
    if step == 'blink':
        if not blink_sequence(scores):
            raise ValueError('眨眼这一步没有正确检测到，请先睁眼、闭眼保持半秒，再睁开双眼')
        return {'passed': True, 'step': step, 'validFrames': len(scores)}
    if step == 'turn_left':
        if len(yaw_scores) < 5:
            raise ValueError('左转这一步画面不足，请重新向左缓慢转头')
        baseline = sorted(yaw_scores[:3])[1]
        deltas = [value - baseline for value in yaw_scores[3:]]
        if not deltas or max(abs(value) for value in deltas) < 0.10:
            raise ValueError('左转这一步没有正确检测到，请保持脸部在框内并明显向左转头')
        direction = 1 if max(deltas) >= abs(min(deltas)) else -1
        return {'passed': True, 'step': step, 'turnDirection': direction, 'validFrames': len(scores)}
    if step == 'turn_right':
        if not head_turn_sequence(yaw_scores):
            raise ValueError('右转这一步没有正确检测到，请从左侧缓慢转向右侧')
        return {'passed': True, 'step': step, 'validFrames': len(scores), 'headTurnRange': round(max(yaw_scores) - min(yaw_scores), 3)}
    raise ValueError('未知的活体检测步骤')


def validate_liveness(images):
    scores, yaw_scores = analyze_frames(images)
    if not blink_sequence(scores):
        raise ValueError('未检测到完整眨眼动作，请先睁眼、闭眼保持半秒，再睁开双眼')
    if not head_turn_sequence(yaw_scores):
        raise ValueError('未检测到完整左右转头动作，请先向左转，再向右转，最后回到正面')
    return {'passed': True, 'method': 'blink_and_head_turn_challenge', 'validFrames': len(scores), 'headTurnRange': round(max(yaw_scores) - min(yaw_scores), 3)}


def validate_blink(images):
    """Compatibility alias retained for existing callers."""
    return validate_liveness(images)
