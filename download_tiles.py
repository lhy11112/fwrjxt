#!/usr/bin/env python3
"""下载西安所有层级的瓦片到后端缓存"""
import math
import subprocess
from concurrent.futures import ThreadPoolExecutor, as_completed

CENTER_LAT = 34.08
CENTER_LON = 108.94
PROXY = "http://localhost:8080/api/v1/tiles"
MAX_WORKERS = 10

# 各缩放级别下载范围 (half_width, half_height: 瓦片数)
GRID = {
    0: 1, 1: 1, 2: 1, 3: 1, 4: 2, 5: 3,
    6: 4, 7: 5, 8: 6, 9: 8, 10: 10,
    11: 12, 12: 15, 13: 20, 14: 25,
    15: 30, 16: 40, 17: 50, 18: 60,
}

def latlon_to_tile(lat, lon, z):
    n = 1 << z
    x = int((lon + 180.0) / 360.0 * n)
    lat_rad = math.radians(lat)
    y = int((1.0 - math.log(math.tan(lat_rad) + 1.0 / math.cos(lat_rad)) / math.pi) / 2.0 * n)
    return x, y

def download_tile(z, x, y):
    url = f"{PROXY}/{z}/{y}/{x}"
    try:
        r = subprocess.run(
            ["curl", "-s", "-o", "/dev/null", "-w", "%{http_code}",
             "--max-time", "15", url],
            capture_output=True, text=True, timeout=20
        )
        return (z, x, y, r.stdout.strip())
    except:
        return (z, x, y, "ERR")

# 收集所有瓦片
all_tiles = []
for z in sorted(GRID.keys()):
    cx, cy = latlon_to_tile(CENTER_LAT, CENTER_LON, z)
    h = GRID[z]
    for dx in range(-h, h + 1):
        for dy in range(-h, h + 1):
            all_tiles.append((z, cx + dx, cy + dy))

total = len(all_tiles)
print(f"西安 ({CENTER_LAT}N, {CENTER_LON}E)")
print(f"共 {total} 个瓦片, zoom 0-18")
print(f"代理: {PROXY}")
print()

# 快速连通性测试
test_url = f"{PROXY}/10/407/820"
r = subprocess.run(["curl", "-s", "-o", "/dev/null", "-w", "%{http_code}", "--max-time", "10", test_url],
                   capture_output=True, text=True)
if r.stdout.strip() != "200":
    print(f"后端不通! {test_url} → {r.stdout.strip()}")
    exit(1)
print("后端连接正常, 开始下载...\n")

completed = 0
ok = 0
fail = 0

with ThreadPoolExecutor(max_workers=MAX_WORKERS) as pool:
    futures = {pool.submit(download_tile, z, x, y): (z, x, y) for z, x, y in all_tiles}
    for f in as_completed(futures):
        z, x, y, code = f.result()
        completed += 1
        if code == "200":
            ok += 1
        else:
            fail += 1
        if completed % 200 == 0 or completed == total:
            pct = completed * 100 // total
            print(f"[{pct}%] {completed}/{total}  OK:{ok}  FAIL:{fail}")

print(f"\n完成! 成功: {ok}, 失败: {fail}")
