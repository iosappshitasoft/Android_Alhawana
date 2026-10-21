import xml.etree.ElementTree as ET
import os

layout_dir = '/Users/apple/AndroidStudioProjects/android_alhawanajd/app/src/main/res/layout'

def print_layout_structure(filename):
    filepath = os.path.join(layout_dir, filename)
    if not os.path.exists(filepath):
        print(f"{filename} does not exist")
        return
    tree = ET.parse(filepath)
    root = tree.getroot()
    
    print(f"\n==================== {filename} ====================")
    
    def traverse(element, depth=0):
        tag = element.tag.split('}')[-1]
        android_ns = '{http://schemas.android.com/apk/res/android}'
        elem_id = element.attrib.get(f'{android_ns}id', '').replace('@+id/', '')
        weight = element.attrib.get(f'{android_ns}layout_weight', '')
        orientation = element.attrib.get(f'{android_ns}orientation', '')
        width = element.attrib.get(f'{android_ns}layout_width', '')
        height = element.attrib.get(f'{android_ns}layout_height', '')
        
        info = []
        if elem_id:
            info.append(f"id:{elem_id}")
        if weight:
            info.append(f"weight:{weight}")
        if orientation:
            info.append(f"orientation:{orientation}")
        if width:
            info.append(f"w:{width}")
        if height:
            info.append(f"h:{height}")
            
        interesting_tags = ['LinearLayout', 'RelativeLayout', 'FrameLayout', 'GridLayout', 'MaterialCardView']
        # Also include any ImageView whose id starts with kickOut to see if kickOut elements are present
        is_kickout = 'ImageView' in tag and elem_id.startswith('kickOut')
        is_mic = 'ImageView' in tag and elem_id.startswith('mic')
        is_diamond = 'LinearLayout' in tag and 'diamond' in elem_id
        
        if any(t in tag for t in interesting_tags) or is_kickout or is_mic or is_diamond:
            print("  " * depth + f"<{tag} {' '.join(info)} />")
            for child in element:
                traverse(child, depth + 1)
            
    traverse(root)

for i in range(10, 16):
    print_layout_structure(f"rv_item{i}.xml")
