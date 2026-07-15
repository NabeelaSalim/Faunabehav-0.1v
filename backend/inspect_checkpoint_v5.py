import torch

ckpt_path = '/Users/karimmaige/Downloads/FaunaBahav/backend/outputs_corrected_v2/faunabehav_r3d18_best.pth'
checkpoint = torch.load(ckpt_path, map_location='cpu', weights_only=False)

model_state = checkpoint['model_state']

print(f"=== model_state keys count: {len(model_state)} ===")

# Print all keys sorted
keys = sorted(model_state.keys())
for k in keys:
    v = model_state[k]
    print(f"  {k}: shape {tuple(v.shape)}")

# Find the classifier/fc layer
print("\n=== Looking for classifier/FC layers ===")
for k in keys:
    if 'fc' in k.lower() or 'classifier' in k.lower() or 'head' in k.lower() or 'linear' in k.lower():
        print(f"  {k}: shape {tuple(model_state[k].shape)}")

# Determine in_features and out_features of fc layer
if 'fc.weight' in model_state:
    out_features, in_features = model_state['fc.weight'].shape
    print(f"\n=== FC layer: in_features={in_features}, out_features={out_features} ===")

# Check for class mapping info in any other key
print("\n=== Other checkpoint info ===")
print(f"  epoch: {checkpoint['epoch']}")
print(f"  best_val_loss: {checkpoint['best_val_loss']}")
print(f"  best_val_f1: {checkpoint['best_val_f1']}")

print("\n=== Optimizer state (first few keys) ===")
opt_state = checkpoint['optimizer_state']
if isinstance(opt_state, dict):
    for k in list(opt_state.keys())[:5]:
        v = opt_state[k]
        if isinstance(v, dict):
            print(f"  {k}: dict with {len(v)} keys")
        elif isinstance(v, torch.Tensor):
            print(f"  {k}: Tensor shape {v.shape}")
        else:
            print(f"  {k}: {type(v).__name__} = {v}")

print("\n=== Scheduler state (first few keys) ===")
sched_state = checkpoint['scheduler_state']
if isinstance(sched_state, dict):
    for k in list(sched_state.keys())[:5]:
        v = sched_state[k]
        if isinstance(v, dict):
            print(f"  {k}: dict with {len(v)} keys")
        elif isinstance(v, torch.Tensor):
            print(f"  {k}: Tensor shape {v.shape}")
        else:
            print(f"  {k}: {type(v).__name__} = {v}")
