from services.inference_service import process_inference

result = process_inference(
    animal="wild_boar",
    behaviour="feeding_foraging",
    confidence=0.91
)

print(result)