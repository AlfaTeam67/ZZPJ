-- Seed dostępnych providerów LLM. Wszystkie wołane przez OpenRouter (OpenAI-compatible API),
-- więc wszystkie używają tego samego klucza API w env: OPENROUTER_API_KEY.
-- Kolejność wyboru sterowana kolumną priority (im niższa wartość, tym wyższy priorytet).

INSERT INTO llm_providers (name, model_id, api_key_env, active, priority)
VALUES
    ('OpenRouter NVIDIA Nemotron 3 Super 120B', 'nvidia/nemotron-3-super-120b-a12b:free', 'OPENROUTER_API_KEY', TRUE, 1),
    ('OpenRouter Google Gemma 3 27B IT',        'google/gemma-4-31b-it:free',             'OPENROUTER_API_KEY', TRUE, 2),
    ('OpenRouter NVIDIA Nemotron 3 Nano Omni',  'google/gemma-4-26b-a4b-it:free', 'OPENROUTER_API_KEY', TRUE, 3),
    ('OpenRouter Auto Router',                  'openrouter/free',                        'OPENROUTER_API_KEY', TRUE, 4);
