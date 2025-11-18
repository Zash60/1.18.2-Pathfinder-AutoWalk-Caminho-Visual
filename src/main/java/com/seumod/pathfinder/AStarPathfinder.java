package com.seumod.pathfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class AStarPathfinder {

    private static final int MAX_SEARCH_NODES = 30000;

    public static List<BlockPos> findPath(BlockPos start, BlockPos target) {
        World world = MinecraftClient.getInstance().world;
        if (world == null) return null;

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<BlockPos, Node> openSetMap = new HashMap<>();
        Set<BlockPos> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0, getHeuristic(start, target));
        openSet.add(startNode);
        openSetMap.put(start, startNode);

        int nodesSearched = 0;

        while (!openSet.isEmpty() && nodesSearched < MAX_SEARCH_NODES) {
            Node currentNode = openSet.poll();
            openSetMap.remove(currentNode.pos);
            nodesSearched++;

            if (currentNode.pos.equals(target)) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode.pos);

            for (Node neighbor : getNeighbors(world, currentNode, target)) {
                if (closedSet.contains(neighbor.pos)) {
                    continue;
                }

                Node existingNode = openSetMap.get(neighbor.pos);
                if (existingNode == null || neighbor.gCost < existingNode.gCost) {
                    if (existingNode != null) {
                        openSet.remove(existingNode);
                    }
                    openSet.add(neighbor);
                    openSetMap.put(neighbor.pos, neighbor);
                }
            }
        }
        return null;
    }

    private static List<Node> getNeighbors(World world, Node currentNode, BlockPos target) {
        List<Node> neighbors = new ArrayList<>();
        BlockPos currentPos = currentNode.pos;

        // --- 1. Vizinhos Adjacentes (Andar, Subir, Cair) ---
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos neighborPos = currentPos.add(dx, 0, dz);
                double movementCost = (dx != 0 && dz != 0) ? 1.414 : 1.0;

                if (isTraversable(world, neighborPos)) {
                    addNeighborNode(neighbors, currentNode, neighborPos, target, movementCost);
                }
                
                BlockPos jumpPos = neighborPos.up();
                if (isTraversable(world, jumpPos) && !isPassable(world, neighborPos)) {
                     addNeighborNode(neighbors, currentNode, jumpPos, target, movementCost + 0.5);
                }

                BlockPos fallCheckPos = currentPos.add(dx, -1, dz);
                if (isTraversable(world, fallCheckPos) && isPassable(world, currentPos.add(dx, 0, dz))) {
                    BlockPos landingPos = findLandingPos(world, fallCheckPos);
                    if (landingPos != null && currentPos.getY() - landingPos.getY() <= 3) {
                        addNeighborNode(neighbors, currentNode, landingPos, target, movementCost);
                    }
                }
            }
        }

        // --- CORREÇÃO: 2. Vizinhos de Salto (Parkour sobre vãos) ---
        // Itera sobre as 4 direções cardeais (Norte, Sul, Leste, Oeste)
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos gapPos = currentPos.offset(direction);      // O bloco do vão
            BlockPos landingPos = gapPos.offset(direction); // O bloco onde vamos aterrissar

            // Condições para o salto:
            // 1. O vão e o espaço acima dele devem estar livres (passable).
            // 2. O local de aterrissagem deve ser seguro (traversable).
            if (isPassable(world, gapPos) && isPassable(world, gapPos.up()) && isTraversable(world, landingPos)) {
                // Adiciona o nó do salto com um custo de movimento de 2 (distância de 2 blocos).
                addNeighborNode(neighbors, currentNode, landingPos, target, 2.0);
            }
        }

        return neighbors;
    }

    private static BlockPos findLandingPos(World world, BlockPos startPos) {
        BlockPos.Mutable currentPos = startPos.mutableCopy();
        for (int i = 0; i < 4; i++) {
            if (isTraversable(world, currentPos)) {
                return currentPos.toImmutable();
            }
            currentPos.move(Direction.DOWN);
        }
        return null;
    }

    private static void addNeighborNode(List<Node> neighbors, Node parent, BlockPos pos, BlockPos target, double movementCost) {
        double gCost = parent.gCost + movementCost;
        double hCost = getHeuristic(pos, target);
        neighbors.add(new Node(pos, parent, gCost, hCost));
    }

    private static boolean isTraversable(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP) &&
               isPassable(world, pos) &&
               isPassable(world, pos.up());
    }
    
    private static boolean isPassable(World world, BlockPos pos) {
        return !world.getBlockState(pos).getMaterial().isSolid();
    }

    private static List<BlockPos> reconstructPath(Node goalNode) {
        List<BlockPos> path = new ArrayList<>();
        Node currentNode = goalNode;
        while (currentNode != null) {
            path.add(currentNode.pos);
            currentNode = currentNode.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private static double getHeuristic(BlockPos from, BlockPos to) {
        return Math.sqrt(from.getSquaredDistance(to));
    }
}
