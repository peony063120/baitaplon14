import os
import sys

# Create directory structure
dirs = [
    r"client\src\main\resources\com\auction\client\view",
    r"client\src\main\resources\com\auction\client\styles"
]

for dir_path in dirs:
    try:
        os.makedirs(dir_path, exist_ok=True)
        print(f"Created: {dir_path}")
    except Exception as e:
        print(f"Error creating {dir_path}: {e}")
        sys.exit(1)

print("All directories created successfully!")
