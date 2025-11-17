# Minecraft 1.18.2 Pathfinder & AutoWalk Mod

Um mod para Minecraft Java 1.18.2 que adiciona navegação automática, pathfinding e um visualizador de caminho.

## ✨ Features

- **Pathfinding Inteligente:** Navega automaticamente até as coordenadas especificadas.
- **Auto-Walk Simples:** Uma tecla para andar para frente continuamente.
- **Comando `/goto`:** Defina um destino com `/goto <x> <y> <z>`.
- **Comando `/stop`:** Cancele qualquer navegação em andamento.
- **Caminho Visual:** Renderiza uma linha no chão mostrando o caminho a ser percorrido.
- **Salto Automático:** Pula obstáculos de 1 bloco de altura automaticamente.
- **Detecção de Obstáculos:** Para automaticamente se o caminho estiver bloqueado por uma parede.

## 🚀 Uso

- `/goto 100 64 -200` - Inicia a navegação para as coordenadas.
- `/stop` - Para toda a navegação e limpa o caminho.
- `P` - Ativa/Desativa o modo Pathfinder (segue o caminho do `/goto`).
- `O` - Ativa/Desativa o AutoWalk simples (apenas anda para frente).

## 🛠️ Build

Para compilar o mod, você pode usar o Gradle Wrapper incluído:
```bash
./gradlew build
