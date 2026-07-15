import sys
import os

# Monkey-patch USE_GLOBAL_DEPS before torch is imported
import torch._utils_internal
torch._utils_internal.USE_GLOBAL_DEPS = False

# Now import torch
import torch

ckpt_path = '/Users/karimmaige/Downloads/FaunaBahav/backend/outputs_corrected_v2/faunabehav_r3d18_best.pth'
checkpoint = torch.load(ckpt_path, map_location='cpu', weights_only=False)

# Print checkpoint keys
print("=== Checkpoint keys ===")
for k in checkpoint.keys():
    print(f"  {k}")

# If it has 'state_dict' or 'model_state_dict', look inside
if isinstance(checkpoint, dict):
    for key in ['state_dict', 'model_state_dict', 'model']:
        if key in checkpoint:
            val = checkpoint[key]
            if isinstance(val, dict):
                print(f"\n=== {key} keys (sample) ===")
                keys = list(val.keys())
                print(f"  Total keys: {len(keys)}")
                for k in keys[:20]:
                    print(f"  {k}")
                # Find classifier layer
                for k in keys:
                    if 'classifier' in k.lower() or 'fc' in k.lower() or 'head' in k.lower():
                        print(f"  >> Classifier key: {k} = shape {val[k].shape}")

# Check if there's a class mapping
if isinstance(checkpoint, dict):
    for key in ['class_names', 'classes', 'idx_to_class', 'class_to_idx', 'labels', 'label_names']:
        if key in checkpoint:
            print(f"\n=== {key} ===")
            print(f"  {checkpoint[key]}")

print("\n=== Checkpoint type ===")
print(type(checkpoint))

# Also print all keys in the checkpoint dict
if isinstance(checkpoint, dict):
    print("\n=== All keys and their types ===")
    for k, v in checkpoint.items():
        if isinstance(v, dict):
            print(f"  {k}: dict with {len(v)} keys")
        elif isinstance(v, list):
            print(f"  {k}: list with {len(v)} items")
        elif isinstance(v, torch.Tensor):
            print(f"  {k}: Tensor of shape {v.shape}")
        else:
            print(f"  {k}: {type(v).__name__} = {v}")

# If the checkpoint itself is a dict-like state dict (not wrapped in a dict with 'state_dict' key)
if isinstance(checkpoint, dict) and all(isinstance(k, str) and ('.' in k or k.startswith('conv') or k.startswith('fc')) for k in list(checkpoint.keys())[:10]):
    print("\n=== Checkpoint IS the state_dict ===")
    keys = list(checkpoint.keys())
    for k in keys:
        if 'fc' in k.lower() or 'classifier' in k.lower() or 'head' in k.lower():
            print(f"  >> {k} = shape {checkpoint[k].shape}")
