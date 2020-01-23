package de.rherzog.master.thesis.slicer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphExporter;

import com.ibm.wala.shrikeBT.IInstruction;
import com.ibm.wala.shrikeCT.InvalidClassFileException;

import de.rherzog.master.thesis.utils.Utilities;

public class BlockDependency {
	private ControlFlow controlFlow;
	private Graph<Block, DefaultEdge> graph;
	private List<List<Block>> simpleCycles;

	public BlockDependency(ControlFlow controlFlowGraph) {
		this.controlFlow = controlFlowGraph;
	}

	public Graph<Block, DefaultEdge> getGraph() throws IOException, InvalidClassFileException {
		if (graph != null) {
			return graph;
		}

		// Build up block graph with vertices
		int blockId = 0;
		IInstruction[] instructions = controlFlow.getMethodData().getInstructions();
		graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		for (int index = 0; index < instructions.length;) {
			// Build up blocks
			Block block = new Block(blockId++);
			graph.addVertex(block);

			IInstruction instruction = instructions[index];
			block.addInstruction(index, instruction);

			// Group subsequent instructions until the stack size equals 0. A block is
			// complete, if the stack is empty (=0) after some instructions.
			int stack = instruction.getPushedWordSize();
			for (index++; index < instructions.length && stack > 0; index++) {
				instruction = instructions[index];
				stack -= Utilities.getPoppedSize(instruction);
				stack += Utilities.getPushedSize(instruction);
				if (stack < 0) {
					throw new java.lang.IllegalStateException("Stack cannot be negative. Is: " + stack);
				}

				block.addInstruction(index, instruction);
			}
//			System.out.println(block);
		}

		// Add edges between the blocks (vertices)
		Graph<Integer, DefaultEdge> cfg = controlFlow.getGraph();
		for (DefaultEdge edge : cfg.edgeSet()) {
			int sourceIndex = cfg.getEdgeSource(edge);
			int targetIndex = cfg.getEdgeTarget(edge);

			Block sourceBlock = getBlockForIndex(sourceIndex);
			Block targetBlock = getBlockForIndex(targetIndex);
			if (sourceBlock != targetBlock) {
				graph.addEdge(sourceBlock, targetBlock);
			}
		}
		return graph;
	}

	public String dotPrint() throws IOException, InvalidClassFileException {
		// use helper classes to define how vertices should be rendered,
		// adhering to the DOT language restrictions
		ComponentNameProvider<Block> vertexIdProvider = new ComponentNameProvider<>() {
			public String getName(Block block) {
				return String.valueOf(block.getId());
			}
		};
		ComponentNameProvider<Block> vertexLabelProvider = new ComponentNameProvider<>() {
			public String getName(Block block) {
				return block.toString();
			}
		};
		GraphExporter<Block, DefaultEdge> exporter = new DOTExporter<>(vertexIdProvider, vertexLabelProvider, null);
		Writer writer = new StringWriter();
		try {
			exporter.exportGraph(getGraph(), writer);
		} catch (ExportException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}

	public List<List<Block>> getSimpleCycles() throws IOException, InvalidClassFileException {
		if (simpleCycles != null) {
			return simpleCycles;
		}

		JohnsonSimpleCycles<Block, DefaultEdge> johnsonSimpleCycles = new JohnsonSimpleCycles<>(getGraph());
		simpleCycles = johnsonSimpleCycles.findSimpleCycles();
		return simpleCycles;
	}

	public Block getBlockForIndex(int index) throws IOException, InvalidClassFileException {
		for (Block block : getGraph().vertexSet()) {
			if (block.getInstructions().containsKey(index)) {
				return block;
			}
		}
		return null;
	}
}
