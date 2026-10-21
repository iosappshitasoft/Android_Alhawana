import re

with open('app/src/main/res/layout/btmsheet_create_new_poll.xml', 'r') as f:
    content = f.read()

# Change Question hint
content = content.replace('android:hint="What color is a sapphire?"', 'android:hint="Ask a question..."')

# Change Add Option icon tint
content = re.sub(r'(android:src="@drawable/baseline_add_24"\s*)app:tint="#888888"', r'\1app:tint="@color/white"', content)

# Update Save Button background
content = content.replace('android:background="@drawable/bg_circle_dark"\n            android:backgroundTint="#1A1C22"', 'android:background="@drawable/bg_save_btn"')

# Add ID to bottom buttons
content = content.replace('<!-- Bottom Buttons -->\n    <LinearLayout', '<!-- Bottom Buttons -->\n    <LinearLayout\n        android:id="@+id/bottomButtonsContainer"')

# Wrap from "Question Section" to end of "durationChipsRow2" in NestedScrollView
start_marker = "    <!-- Question Section -->"
end_marker = "    <!-- Bottom Buttons -->"

parts = content.split(start_marker)
top_part = parts[0]
rest = parts[1].split(end_marker)
middle_part = start_marker + rest[0]
bottom_part = end_marker + rest[1]

# Adjust middle constraints to be linear layout compatible
middle_part = middle_part.replace('app:layout_constraintStart_toStartOf="parent"', '')
middle_part = middle_part.replace('app:layout_constraintEnd_toEndOf="parent"', '')
middle_part = re.sub(r'app:layout_constraintTop_toBottomOf="@[^"]+"', '', middle_part)

# Wrap middle part
wrapped_middle = f"""    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        android:layout_marginBottom="16dp"
        app:layout_constraintTop_toBottomOf="@id/btnBack"
        app:layout_constraintBottom_toTopOf="@id/bottomButtonsContainer">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingBottom="16dp">
        
{middle_part}
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
"""

# Update bottom constraint
bottom_part = re.sub(r'app:layout_constraintTop_toBottomOf="@[^"]+"', 'app:layout_constraintBottom_toBottomOf="parent"', bottom_part)

final_content = top_part + wrapped_middle + bottom_part

with open('app/src/main/res/layout/btmsheet_create_new_poll.xml', 'w') as f:
    f.write(final_content)

