import zipfile
import pickle
import sys

ckpt_path = '/Users/karimmaige/Downloads/FaunaBahav/backend/outputs_corrected_v2/faunabehav_r3d18_best.pth'

# Extract data.pkl and load it with pickle
with zipfile.ZipFile(ckpt_path, 'r') as z:
    with z.open('faunabehav_r3d18_best/data.pkl') as f:
        data = pickle.load(f)

print("=== Pickle data type ===")
print(type(data))

if isinstance(data, dict):
    print(f"\n=== Dict keys ===")
    for k in data.keys():
        print(f"  {k}")
    
    for k, v in data.items():
        if isinstance(v, dict):
            print(f"\n  '{k}' is a dict with {len(v)} keys")
            subkeys = list(v.keys())
            print(f"  Sample keys: {subkeys[:10]}")
            for sk in subkeys:
                sv = v[sk]
                if hasattr(sv, 'shape'):
                    print(f"    {sk}: shape {sv.shape}")
        elif hasattr(v, 'shape'):
            print(f"\n  '{k}': Tensor of shape {v.shape}")
        elif isinstance(v, list):
            print(f"\n  '{k}': list of len {len(v)}")
        else:
            print(f"\n  '{k}': {type(v).__name__} = {v}")
    
    # Look for class names/idx_to_class
    for key in ['class_names', 'classes', 'idx_to_class', 'class_to_idx', 'labels', 'label_names', 'label_mapping', 'unique_labels', 'num_classes']:
        if key in data:
            print(f"\n=== {key} ===")
            print(f"  {data[key]}")

# Also try extracting the storage to understand model structure
with zipfile.ZipFile(ckpt_path, 'r') as z:
    # Read version
    with z.open('faunabehav_r3d18_best/version') as f:
        print(f"\n=== Version ===")
        print(f"  {f.read().decode('utf-8')}")
    
    # Try to read the pickle from storage (if it's a state_dict)
    with z.open('faunabehav_r3d18_best/data/0') as f:
        storage_data = f.read()
        print(f"\n=== Storage data/0: {len(storage_data)} bytes ===")
