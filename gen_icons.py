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

# 1 Guest
save('ic_layout_1_guest', [
    {"left": 6, "right": 32, "top": 6, "bottom": 6, "color": "#33FFFFFF"},
    {"left": 32, "right": 6, "top": 6, "bottom": 6, "color": "#1AFFFFFF"}
])

# 3 Guests
save('ic_layout_3_guests', [
    {"left": 6, "right": 32, "top": 6, "bottom": 6, "color": "#33FFFFFF"},
    {"left": 32, "right": 6, "top": 6, "bottom": 34, "color": "#1AFFFFFF"},
    {"left": 32, "right": 6, "top": 23, "bottom": 17, "color": "#1AFFFFFF"},
    {"left": 32, "right": 6, "top": 40, "bottom": 6, "color": "#1AFFFFFF"}
])

# 6 Guests
# Left box: w=28, right=60-6-28=26.
# 6 boxes on right: w=16. left=60-6-16=38.
# Heights: total 48. 6 boxes = 6*h + 5*2 = 48 -> 6h = 38 -> h=6.33. Let's use 6dp height, 2dp gap -> 46dp.
save('ic_layout_6_guests', [
    {"left": 6, "right": 26, "top": 6, "bottom": 6, "color": "#33FFFFFF"},
    *[{"left": 38, "right": 6, "top": 6 + i*8, "bottom": 60 - (6 + i*8 + 6), "color": "#1AFFFFFF"} for i in range(6)]
])

# 9 Guests (3x3 grid)
# 60dp total. padding 6dp. 48x48 working area.
# 3 boxes across: (48-4)/3 = 14.6dp. Let's use 14dp size, 3dp gap.
# 14*3 + 3*2 = 48.
grid_9 = []
for row in range(3):
    for col in range(3):
        t = 6 + row*17
        b = 60 - (t + 14)
        l = 6 + col*17
        r = 60 - (l + 14)
        grid_9.append({"left": l, "right": r, "top": t, "bottom": b, "color": "#1AFFFFFF" if col>0 or row>0 else "#33FFFFFF"})
save('ic_layout_9_guests', grid_9)

# 16 Guests (4x4 grid)
# 4 boxes across: (48-6)/4 = 10.5dp. Let's use 10dp size, 2dp gap.
# 10*4 + 2*3 = 46. (leave 7dp padding instead of 6)
grid_16 = []
for row in range(4):
    for col in range(4):
        t = 7 + row*12
        b = 60 - (t + 10)
        l = 7 + col*12
        r = 60 - (l + 10)
        grid_16.append({"left": l, "right": r, "top": t, "bottom": b, "color": "#1AFFFFFF" if col>0 or row>0 else "#33FFFFFF"})
save('ic_layout_16_guests', grid_16)

