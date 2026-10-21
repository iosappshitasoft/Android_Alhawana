import xml.etree.ElementTree as ET
import os

layout_dir = '/Users/apple/AndroidStudioProjects/android_alhawanajd/app/src/main/res/layout'

def print_clean_hierarchy(filename):
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
            
        interesting_tags = ['LinearLayout', 'MaterialCardView']
        
        if tag in interesting_tags:
            print("  " * depth + f"<{tag} {' '.join(info)}>")
            for child in element:
                traverse(child, depth + 1)
            print("  " * depth + f"</{tag}>")
        else:
            for child in element:
                traverse(child, depth)
            
    traverse(root)

for i in range(10, 17):
    print_clean_hierarchy(f"rv_item{i}.xml")
