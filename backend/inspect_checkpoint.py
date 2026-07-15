import torch
import sys

checkpoint = torch.load('outputs_corrected_v2/faunabehav_r3d18_best.pth', map_location='cpu', weights_only=False)
print('=== Checkpoint type:', type(checkpoint))

if isinstance(checkpoint, dict):
    print('=== Top-level keys:', list(checkpoint.keys()))
    
    for key in checkpoint.keys():
        val = checkpoint[key]
        if isinstance(val, dict):
            keys_list = list(val.keys())
            print(f'\n--- {key}: dict with {len(keys_list)} keys ---')
            # Show first 5 and search for classifier/fc/head
            for k in keys_list[:5]:
                v = val[k]
                if hasattr(v, 'shape'):
                    print(f'  {k}: shape {v.shape}')
                else:
                    print(f'  {k}: {type(v).__name__}')
            
            if len(keys_list) > 5:
                print(f'  ... ({len(keys_list) - 5} more keys)')
            
            # Find fc/classifier/head layers
            for k in keys_list:
                kl = k.lower()
                if any(x in kl for x in ['fc', 'classifier', 'head', 'linear']):
                    v = val[k]
                    if hasattr(v, 'shape'):
                        print(f'  >> CLASSIFIER: {k} -> shape {v.shape}')
        elif hasattr(val, 'shape'):
            print(f'\n--- {key}: tensor shape {val.shape} ---')
        else:
            print(f'\n--- {key}: {type(val).__name__} = {str(val)[:200]} ---')

# Check for class mappings
if isinstance(checkpoint, dict):
    for key in ['class_names', 'classes', 'idx_to_class', 'class_to_idx', 'labels', 'label_names', 'class_mapping', 'species', 'behaviours']:
        if key in checkpoint:
            print(f'\n=== CLASS MAPPING: {key} = {checkpoint[key]} ===')
