import unittest
from unittest.mock import patch
from blink_detection import blink_sequence, head_turn_sequence, validate_liveness_step


class BlinkSequenceTest(unittest.TestCase):
    def test_full_bilateral_blink(self):
        self.assertTrue(blink_sequence([(0.1, 0.1)]*3+[(0.9, 0.9)]*3+[(0.1, 0.1)]*3))

    def test_static_open_or_closed_eyes_fail(self):
        self.assertFalse(blink_sequence([(0.1, 0.1)]*20))
        self.assertFalse(blink_sequence([(0.9, 0.9)]*20))

    def test_wink_and_single_noisy_frame_fail(self):
        self.assertFalse(blink_sequence([(0.1, 0.1)]*3+[(0.9, 0.1)]*3+[(0.1, 0.1)]*3))
        self.assertFalse(blink_sequence([(0.1, 0.1)]*3+[(0.65, 0.65)]+[(0.1, 0.1)]*3))

    def test_one_strong_closed_frame_accepts_a_natural_blink(self):
        self.assertTrue(blink_sequence([(0.1, 0.1)]*3+[(0.9, 0.9)]+[(0.1, 0.1)]*3))

    def test_must_reopen(self):
        self.assertFalse(blink_sequence([(0.1, 0.1)]*3+[(0.9, 0.9)]*3))

    def test_head_turns_to_both_sides(self):
        self.assertTrue(head_turn_sequence([0.02, 0.01, 0.02, 0.15, 0.21, 0.12, -0.13, -0.20, -0.12, 0.01]))

    def test_one_sided_or_stationary_head_fails(self):
        self.assertFalse(head_turn_sequence([0.01] * 10))
        self.assertFalse(head_turn_sequence([0.01, 0.02, 0.01, 0.14, 0.20, 0.16, 0.05, 0.02]))

    def test_blink_gate_blocks_before_next_step(self):
        with patch('blink_detection.analyze_frames', return_value=([(0.1, 0.1)] * 12, [0.0] * 12)):
            with self.assertRaisesRegex(ValueError, '眨眼这一步'):
                validate_liveness_step([object()] * 12, 'blink')

    def test_turn_gates_require_first_and_opposite_turns(self):
        first_turn = [0.0, 0.01, 0.0, 0.05, 0.16, 0.20]
        with patch('blink_detection.analyze_frames', return_value=([(0.1, 0.1)] * 6, first_turn)):
            self.assertTrue(validate_liveness_step([object()] * 6, 'turn_left')['passed'])
        with patch('blink_detection.analyze_frames', return_value=([(0.1, 0.1)] * 8, first_turn + [0.08, 0.02])):
            with self.assertRaisesRegex(ValueError, '右转这一步'):
                validate_liveness_step([object()] * 8, 'turn_right')


if __name__ == '__main__':
    unittest.main()
