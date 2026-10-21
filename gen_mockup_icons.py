def generate_layer_list(items):
    out = ['<?xml version="1.0" encoding="utf-8"?>']
    out.append('<layer-list xmlns:android="http://schemas.android.com/apk/res/android">')
    for item in items:
        out.append(f'    <item android:left="{item["left"]}dp" android:right="{item["right"]}dp" android:top="{item["top"]}dp" android:bottom="{item["bottom"]}dp">')
        out.append('        <shape android:shape="rectangle">')
        out.append(f'            <solid android:color="{item["color"]}" />')
        out.append('            <corners android:radius="4dp" />') # use 4dp for big mockup
        out.append('        </shape>')
        out.append('    </item>')
    out.append('</layer-list>')
    return "\n".join(out)

def save(name, items):
    with open(f'app/src/main/res/drawable/{name}.xml', 'w') as f:
        f.write(generate_layer_list(items))

COLOR_HOST = "#8D0A87"
COLOR_GUEST = "#D858FA"

W = 140
H = 200

# Default Selected (1 big box)
save('ic_layout_mockup_default_selected', [
    {"left": 4, "right": 4, "top": 4, "bottom": 4, "color": COLOR_HOST}
])

# 1 Guest
# 2 vertical boxes. W=140. Gap=4. 
# Box W = (140-12)/2 = 64.
# Left box: left=4, right=140-4-64 = 72
# Right box: left=4+64+4=72, right=4
save('ic_layout_mockup_1_guest_selected', [
    {"left": 4, "right": 72, "top": 4, "bottom": 4, "color": COLOR_HOST},
    {"left": 72, "right": 4, "top": 4, "bottom": 4, "color": COLOR_GUEST}
])

# 3 Guests
# Right side has 3 boxes. Height=200. Gap=4.
# Box H = (200-4*4)/3 = 184/3 = 61.33. Let's use 61.
# 61*3 + 8 = 191. Total=200. Leftover=1. We'll adjust bottom paddings.
box_h = 61
gap = 4
items_3 = [{"left": 4, "right": 72, "top": 4, "bottom": 4, "color": COLOR_HOST}]
for i in range(3):
    t = 4 + i*(box_h + gap)
    b = H - (t + box_h)
    if i == 2: b = 4 # stretch last box slightly to fill
    items_3.append({"left": 72, "right": 4, "top": t, "bottom": b, "color": COLOR_GUEST})
save('ic_layout_mockup_3_guests_selected', items_3)

# 6 Guests
# Right side has 6 boxes.
# Box H = (200 - 4 - 4 - 5*4)/6 = 172/6 = 28.66. Let's use 28.
box_h_6 = 28
items_6 = [{"left": 4, "right": 72, "top": 4, "bottom": 4, "color": COLOR_HOST}]
for i in range(6):
    t = 4 + i*(box_h_6 + gap)
    b = H - (t + box_h_6)
    if i == 5: b = 4
    items_6.append({"left": 72, "right": 4, "top": t, "bottom": b, "color": COLOR_GUEST})
save('ic_layout_mockup_6_guests_selected', items_6)

# 9 Guests (3x3 grid)
# W=140. 3 cols. Box W = (140 - 4*4)/3 = 124/3 = 41.33 -> 41.
# H=200. 3 rows. Box H = (200 - 4*4)/3 = 184/3 = 61.33 -> 61.
items_9 = []
for row in range(3):
    for col in range(3):
        t = 4 + row*(61 + gap)
        b = H - (t + 61)
        if row == 2: b = 4
        
        l = 4 + col*(41 + gap)
        r = W - (l + 41)
        if col == 2: r = 4
        
        is_host = (col == 0 and row == 0)
        items_9.append({"left": l, "right": r, "top": t, "bottom": b, "color": COLOR_GUEST if not is_host else COLOR_HOST})
save('ic_layout_mockup_9_guests_selected', items_9)

# 16 Guests (4x4 grid)
# 4 cols. Box W = (140 - 5*4)/4 = 120/4 = 30.
# 4 rows. Box H = (200 - 5*4)/4 = 180/4 = 45.
items_16 = []
for row in range(4):
    for col in range(4):
        t = 4 + row*(45 + gap)
        b = H - (t + 45)
        if row == 3: b = 4
        
        l = 4 + col*(30 + gap)
        r = W - (l + 30)
        if col == 3: r = 4
        
        is_host = (col == 0 and row == 0)
        items_16.append({"left": l, "right": r, "top": t, "bottom": b, "color": COLOR_GUEST if not is_host else COLOR_HOST})
save('ic_layout_mockup_16_guests_selected', items_16)

