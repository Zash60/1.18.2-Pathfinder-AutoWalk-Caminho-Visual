package com.seumod.pathfinder;

import net.minecraft.util.math.BlockPos;

// Classe que representa um nó na busca A*.
public class Node implements Comparable<Node> {
    public final BlockPos pos;
    public Node parent;
    public double gCost; // Custo do início até este nó
    public double hCost; // Heurística: custo estimado deste nó até o fim
    public double fCost; // Custo total (gCost + hCost)

    public Node(BlockPos pos, Node parent, double gCost, double hCost) {
        this.pos = pos;
        this.parent = parent;
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = gCost + hCost;
    }

    // Usado pela PriorityQueue para sempre pegar o nó com menor fCost.
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.fCost, other.fCost);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return pos.equals(node.pos);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }
}
