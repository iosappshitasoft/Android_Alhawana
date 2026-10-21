def generate_layer_list(items):
    out = ['<?xml version="1.0" encoding="utf-8"?>']
    out.append('<layer-list xmlns:android="http://schemas.android.com/apk/res/android">')
    for item in items:
        out.append(f'    <item android:left="{item["left"]}dp" android:right="{item["right"]}dp" android:top="{item["top"]}dp" android:bottom="{item["bottom"]}dp">')
        out.append('        <shape android:shape="rectangle">')
        out.append(f'            <solid android:color="{item["color"]}" />')
        out.append('            <corners android:radius="2dp" />')
        out.append('        </shape>')
        out.append('    </item>')
    out.append('</layer-list>')
    return "\n".join(out)

def save(name, items):
    with open(f'app/src/main/res/drawable/{name}.xml', 'w') as f:
        f.write(generate_layer_list(items))

COLOR_HOST = "#8D0A87"
COLOR_GUEST = "#D858FA"

# Default Selected (just 1 big host)
save('ic_layout_default_selected', [
    {"left": 6, "right": 6, "top": 6, "bottom": 6, "color": COLOR_HOST}
])

# 1 Guest Selected
save('ic_layout_1_guest_selected', [
    {"left": 6, "right": 32, "top": 6, "bottom": 6, "color": COLOR_HOST},
    {"left": 32, "right": 6, "top": 6, "bottom": 6, "color": COLOR_GUEST}
])

# 3 Guests Selected
save('ic_layout_3_guests_selected', [
    {"left": 6, "right": 32, "top": 6, "bottom": 6, "color": COLOR_HOST},
    {"left": 32, "right": 6, "top": 6, "bottom": 34, "color": COLOR_GUEST},
    {"left": 32, "right": 6, "top": 23, "bottom": 17, "color": COLOR_GUEST},
    {"left": 32, "right": 6, "top": 40, "bottom": 6, "color": COLOR_GUEST}
])

# 6 Guests Selected
save('ic_layout_6_guests_selected', [
    {"left": 6, "right": 26, "top": 6, "bottom": 6, "color": COLOR_HOST},
    *[{"left": 38, "right": 6, "top": 6 + i*8, "bottom": 60 - (6 + i*8 + 6), "color": COLOR_GUEST} for i in range(6)]
])

# 9 Guests Selected
grid_9 = []
for row in range(3):
    for col in range(3):
        t = 6 + row*17
        b = 60 - (t + 14)
        l = 6 + col*17
        r = 60 - (l + 14)
        is_host = (col == 0 and row == 0)
        grid_9.append({"left": l, "right": r, "top": t, "bottom": b, "color": COLOR_GUEST if not is_host else COLOR_HOST})
save('ic_layout_9_guests_selected', grid_9)

# 16 Guests Selected
grid_16 = []
for row in range(4):
    for col in range(4):
        t = 7 + row*12
        b = 60 - (t + 10)
        l = 7 + col*12
        r = 60 - (l + 10)
        is_host = (col == 0 and row == 0)
        grid_16.append({"left": l, "right": r, "top": t, "bottom": b, "color": COLOR_GUEST if not is_host else COLOR_HOST})
save('ic_layout_16_guests_selected', grid_16)

