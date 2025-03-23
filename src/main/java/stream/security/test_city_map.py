import json
import unittest
from parameterized import parameterized

from city_map import CityMap

def load_test_cases():
    with open("test_cases.json") as f:
        return [(tc["tc_id"], tc["src"], tc["dst"], tc["expect_existence"], tc["expected_path"]) for tc in json.load(f)]

all_test_cases = load_test_cases()
test_cases_expect_success = [tc for tc in all_test_cases if tc[3]]
test_cases_expect_failure = [tc for tc in all_test_cases if not tc[3]]

class TestCityMap(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        with open("map_entities.json") as f:
            entities = json.load(f)
            cls.city_map = CityMap(entities)

    def check_existence(self, src, dst, actual_path, expect_to_exist):
        succeed = len(actual_path) >= 2 and src == actual_path[0] and dst == actual_path[-1]
        if expect_to_exist:
            self.assertTrue(succeed,
                            f"[Path Exists] {src} → {dst} expected to exist, but got {json.dumps(actual_path)}")
        else:
            self.assertFalse(succeed,
                             f"[Path Exists] {src} → {dst} expected to not exist, but got {json.dumps(actual_path)}")

    def check_correctness(self, actual_path, dst, expected_path, src):
        self.assertEqual(
            expected_path, actual_path,
            f"[Path Correct] {src} → {dst} expected {expected_path}, but got {json.dumps(actual_path)}"
        )

    def check_longest_path(self, actual_path, dst, longest_path, src):
        self.assertEqual(
            longest_path, actual_path,
            f"[Path Correct] {src} → {dst} expected {longest_path}, but got {json.dumps(actual_path)}"
        )

    @parameterized.expand(all_test_cases, testcase_func_name=lambda f, n,
                                                                       p: f"{f.__name__}: {p.args[1]} -> {p.args[2]}, expect {p.args[3]}")
    def test_a_path_existence(self, tc_id, src, dst, expect_existence, expected_path):
        actual_path = self.city_map.get_path(src, dst)
        self.check_existence(src, dst, actual_path, expect_existence)
        print(f"Test case {tc_id} passed for path existence")

    @parameterized.expand(test_cases_expect_success, testcase_func_name=lambda f, n, p: f"{f.__name__}: {p.args[1]} -> {p.args[2]}, expect {p.args[4]}")
    def test_b_path_correctness(self, tc_id, src, dst, expect_existence, expected_path):
        actual_path = self.city_map.get_path(src, dst)
        self.check_correctness(actual_path, dst, expected_path, src)
        print(f"Test case {tc_id} passed for path correctness")

    @parameterized.expand(test_cases_expect_failure, testcase_func_name=lambda f, n, p: f"{f.__name__}: {p.args[1]} -> {p.args[2]}, expect {p.args[4]}")
    def test_c_longest_path(self, tc_id, src, dst, expect_existence, expected_path):
        actual_path = self.city_map.get_path(src, dst)
        self.check_longest_path(actual_path, dst, expected_path, src)
        print(f"Test case {tc_id} passed for path failure")







