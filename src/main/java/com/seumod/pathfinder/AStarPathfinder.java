package com.seumod.pathfinder;

import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class AStarPathfinder {

    private static final int MAX_SEARCH_NODES = 30000; // Limite para evitar busca infinita

    public static List<BlockPos> findPath(BlockPos start, BlockPos target) {
        World world = MinecraftClient.getInstance().world;
        if (world == null) return null;

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<BlockPos> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0, getHeuristic(start, target));
        openSet.add(startNode);

        int nodesSearched = 0;

        while (!openSet.isEmpty() && nodesSearched < MAX_SEARCH_NODES) {
            Node currentNode = openSet.poll();
            nodesSearched++;

            if (currentNode.pos.equals(target)) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode.pos);

            for (Node neighbor : getNeighbors(world, currentNode, target)) {
                if (closedSet.contains(neighbor.pos)) {
                    continue;
                }

                if (!openSet.contains(neighbor) || neighbor.gCost < getGcostFromOpenSet(openSet, neighbor)) {
                    openSet.remove(neighbor); // Remove para atualizar com o novo custo
                    openSet.add(neighbor);
                }
            }
        }
        return null; // Caminho não encontrado
    }
    
    private static double getGcostFromOpenSet(PriorityQueue<Node> openSet, Node targetNode) {
        for(Node node : openSet) {
            if(node.equals(targetNode)) {
                return node.gCost;
            }
        }
        return Double.MAX_VALUE;
    }

    private static List<Node> getNeighbors(World world, Node currentNode, BlockPos target) {
        List<Node> neighbors = new ArrayList<>();
        BlockPos currentPos = currentNode.pos;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos neighborPos = currentPos.add(dx, 0, dz);

                // Movimento normal no chão
                if (isTraversable(world, neighborPos)) {
                    addNeighborNode(neighbors, currentNode, neighborPos, target);
                }
                
                // Movimento de subida (salto)
                BlockPos jumpPos = neighborPos.up();
                if (isTraversable(world, jumpPos) && !isTraversable(world, neighborPos)) {
                     addNeighborNode(neighbors, currentNode, jumpPos, target);
                }

                // Movimento de descida (queda segura)
                BlockPos fallPos = neighborPos.down();
                if (isTraversable(world, neighborPos) && !isTraversable(world, fallPos) && isSafeFall(world, fallPos)) {
                     addNeighborNode(neighbors, currentNode, neighborPos, target);
                }
            }
        }
        return neighbors;
    }

    private static void addNeighborNode(List<Node> neighbors, Node parent, BlockPos pos, BlockPos target) {
        double distance = parent.pos.isAdjacent(pos, 1.1) ? 1.0 : 1.4; // Custo maior para diagonais
        double gCost = parent.gCost + distance;
        double hCost = getHeuristic(pos, target);
        neighbors.add(new Node(pos, parent, gCost, hCost));
    }

    // Verifica se o jogador pode ficar nesta posição (2 blocos de ar)
    private static boolean isTraversable(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSolidBlock() &&
               world.getBlockState(pos).getMaterial() == Material.AIR &&
               world.getBlockState(pos.up()).getMaterial() == Material.AIR;
    }

    // Verifica se a queda é segura (máximo de 3 blocos)
    private static boolean isSafeFall(World world, BlockPos pos) {
        for (int i = 0; i <= 3; i++) {
            BlockPos checkPos = pos.down(i);
            if(isTraversable(world, checkPos)) {
                return true;
            }
        }
        return false;
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
        // Distância Euclidiana é uma boa heurística para Minecraft
        return Math.sqrt(from.getSquaredDistance(to));
    }
}
