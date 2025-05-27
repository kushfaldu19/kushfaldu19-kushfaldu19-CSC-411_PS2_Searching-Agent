package edu.ncsu.csc411.ps02.agent;

import java.util.*;

import edu.ncsu.csc411.ps02.environment.Tile;
import edu.ncsu.csc411.ps02.environment.TileStatus;
import edu.ncsu.csc411.ps02.environment.Action;
import edu.ncsu.csc411.ps02.environment.Environment;
import edu.ncsu.csc411.ps02.environment.Position;

public class Robot {
    private Environment env; // Reference to the environment in which the robot operates

    // Constructor to initialize the robot with the given environment
    public Robot(Environment env) {
        this.env = env;
    }

    // Determines the action the robot should take in the current time step
    public Action getAction() {
        Position selfPos = env.getRobotPosition(this); // Get the robot's current position
        Position targetPos = env.getTarget(); // Get the target position

        // If the robot is already at the target, do nothing
        if (selfPos.equals(targetPos)) {
            return Action.DO_NOTHING;
        }

        // PriorityQueue for managing nodes during A* search, sorted by fCost (total cost)
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(n -> n.fCost));
        Set<Position> closedList = new HashSet<>(); // Set to keep track of visited positions

        // Add the starting position to the open list
        openList.add(new Node(selfPos, null, 0, calculateHeuristic(selfPos, targetPos)));

        // Process nodes until the open list is empty
        while (!openList.isEmpty()) {
            Node currentNode = openList.poll(); // Get the node with the lowest fCost
            closedList.add(currentNode.position); // Mark the current node as visited

            // If the target is reached, reconstruct the path to determine the action
            if (currentNode.position.equals(targetPos)) {
                return reconstructAction(currentNode);
            }

            // Get neighboring positions of the current node
            Map<String, Position> neighbors = env.getNeighborPositions(currentNode.position);
            for (Map.Entry<String, Position> entry : neighbors.entrySet()) {
                Position neighborPos = entry.getValue();

                // Skip neighbors that are null, already visited, or impassable
                if (neighborPos == null || closedList.contains(neighborPos) || env.getTiles().get(neighborPos).getStatus() == TileStatus.IMPASSABLE) {
                    continue;
                }

                // Calculate costs for the neighbor
                int gCost = currentNode.gCost + 1; // Cost from start to this neighbor
                int hCost = calculateHeuristic(neighborPos, targetPos); // Estimated cost to target
                Node neighborNode = new Node(neighborPos, currentNode, gCost, hCost);

                // Skip if a better path to this neighbor already exists in the open list
                boolean skip = openList.stream().anyMatch(n -> n.position.equals(neighborPos) && n.fCost <= neighborNode.fCost);
                if (!skip) {
                    openList.add(neighborNode); // Add the neighbor to the open list
                }
            }
        }

        // If no path is found, do nothing
        return Action.DO_NOTHING;
    }

    // Calculates the heuristic (Manhattan distance) between two positions
    private int calculateHeuristic(Position pos, Position target) {
        return Math.abs(pos.getRow() - target.getRow()) + Math.abs(pos.getCol() - target.getCol());
    }

    // Reconstructs the first action from the path leading to the target
    private Action reconstructAction(Node node) {
        // Backtrack from the target to determine the first move
        Node current = node;
        while (current.parent != null && current.parent.parent != null) {
            current = current.parent;
        }

        // Determine the direction of the first move based on positions
        Position startPos = current.parent.position; // Current position
        Position nextPos = current.position; // Next position

        if (nextPos.getRow() < startPos.getRow()) return Action.MOVE_UP;
        if (nextPos.getRow() > startPos.getRow()) return Action.MOVE_DOWN;
        if (nextPos.getCol() < startPos.getCol()) return Action.MOVE_LEFT;
        if (nextPos.getCol() > startPos.getCol()) return Action.MOVE_RIGHT;

        return Action.DO_NOTHING; // Default action if no valid move is found
    }

    // Represents a node in the search tree for A* search
    private static class Node {
        Position position; // Current position of the node
        Node parent; // Parent node in the path
        int gCost; // Cost from the start to this node
        int hCost; // Heuristic cost from this node to the target
        int fCost; // Total cost (gCost + hCost)

        Node(Position position, Node parent, int gCost, int hCost) {
            this.position = position;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost; // Calculate total cost
        }
    }
}
