import json


class CityMap:
    def __init__(self, resources_by_type: dict):
        raise NotImplementedError

    def get_path(self, src_id: str, dst_id: str) -> list[str]:
        raise NotImplementedError


if __name__ == '__main__':
    with open('map_entities.json') as f:
        entities = json.load(f)
        city_map = CityMap(entities)
        print(city_map.get_path("house-5", "house-2"))
