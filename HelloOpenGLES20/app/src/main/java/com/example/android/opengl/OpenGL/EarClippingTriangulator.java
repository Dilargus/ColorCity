package com.example.android.opengl.OpenGL;

import com.example.android.opengl.OSM.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * @author Woess
 * A class for generating a triangulation of a 2D - Polygon using an earclipping algorithm
 * call functions: 	computeTriangles(final List<Node> polygon) -> for any polygon
 * 					triangulateRectangle(ArrayList<Node> nodes) -> faster, but just for convex polygons
 * gets called by class OSMManager
 */
public final class EarClippingTriangulator {

	private static final int CONCAVE = 1;
	private static final int CONVEX = -1;

	private int concave_vertex_count;

	public ArrayList<Node> computeTriangles(final List<Node> polygon) {
		final ArrayList<Node> triangles = new ArrayList<Node>();
		final ArrayList<Node> vertices = new ArrayList<Node>(polygon.size());
		vertices.addAll(polygon);

		if (vertices.size() == 3) {
			triangles.addAll(vertices);
			return triangles;
		}

		while (vertices.size() >= 3) {
			final int vertex_types[] = this.classifyVertices(vertices);

			final int vertex_count = vertices.size();
			for (int index = 0; index < vertex_count; index++) {
				if (this.isEarTip(vertices, index, vertex_types)) {
					this.cutEarTip(vertices, index, triangles);
					break;
				}
			}
		}

		return triangles;
	}

	public ArrayList<Node> triangulateRectangle(ArrayList<Node> nodes) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (int i = 1; i < nodes.size() - 1; i++) {
			result.add(nodes.get(0));
			result.add(nodes.get(i));
			result.add(nodes.get(i + 1));
		}
		return result;
	}

	private static boolean areVerticesClockwise(final ArrayList<Node> p_vertices) {
		final int vertex_count = p_vertices.size();

		double area = 0;
		for (int i = 0; i < vertex_count; i++) {
			final Node p1 = p_vertices.get(i);
			final Node p2 = p_vertices.get(computeNextIndex(p_vertices, i));
			area += p1.x * p2.y - p2.x * p1.y;
		}

		if (area < 0) {
			return true;
		} else {
			return false;
		}
	}

	private int[] classifyVertices(final ArrayList<Node> p_vertices) {
		final int vertex_count = p_vertices.size();

		final int[] vertex_types = new int[vertex_count];
		this.concave_vertex_count = 0;

		/* Ensure vertices are in clockwise order. */
		if (!areVerticesClockwise(p_vertices)) {
			Collections.reverse(p_vertices);
		}

		for (int index = 0; index < vertex_count; index++) {
			final int previous_i = EarClippingTriangulator
					.computePreviousIndex(p_vertices, index);
			final int next_i = EarClippingTriangulator.computeNextIndex(
					p_vertices, index);

			final Node previous_v = p_vertices.get(previous_i);
			final Node current_v = p_vertices.get(index);
			final Node next_v = p_vertices.get(next_i);

			if (isTriangleConvex(previous_v.x, previous_v.y, current_v.x,
					current_v.y, next_v.x, next_v.y)) {
				vertex_types[index] = CONVEX;
			} else {
				vertex_types[index] = CONCAVE;
				this.concave_vertex_count++;
			}
		}

		return vertex_types;
	}

	private static boolean isTriangleConvex(final double pX1, final double pY1,
			final double pX2, final double pY2, final double pX3,
			final double pY3) {
		if (computeSpannedAreaSign(pX1, pY1, pX2, pY2, pX3, pY3) < 0) {
			return false;
		} else {
			return true;
		}
	}

	private static int computeSpannedAreaSign(final double pX1,
			final double pY1, final double pX2, final double pY2,
			final double pX3, final double pY3) {
		double area = 0;

		area += pX1 * (pY3 - pY2);
		area += pX2 * (pY1 - pY3);
		area += pX3 * (pY2 - pY1);

		return (int) Math.signum(area);
	}

	private static boolean isAnyVertexInTriangle(
			final ArrayList<Node> p_vertices, final int[] p_vertex_types,
			final double pX1, final double pY1, final double pX2,
			final double pY2, final double pX3, final double pY3) {
		int i = 0;

		final int vertex_count = p_vertices.size();
		while (i < vertex_count - 1) {
			if ((p_vertex_types[i] == CONCAVE)) {
				final Node current_v = p_vertices.get(i);

				final double current_vx = current_v.x;
				final double current_vy = current_v.y;

				final int area_sign1 = computeSpannedAreaSign(pX1, pY1, pX2,
						pY2, current_vx, current_vy);
				final int area_sign2 = computeSpannedAreaSign(pX2, pY2, pX3,
						pY3, current_vx, current_vy);
				final int area_sign3 = computeSpannedAreaSign(pX3, pY3, pX1,
						pY1, current_vx, current_vy);

				if (area_sign1 > 0 && area_sign2 > 0 && area_sign3 > 0) {
					return true;
				} else if (area_sign1 <= 0 && area_sign2 <= 0
						&& area_sign3 <= 0) {
					return true;
				}
			}
			i++;
		}
		return false;
	}

	private boolean isEarTip(final ArrayList<Node> p_vertices,
			final int p_eartip_i, final int[] p_vertex_types) {
		if (this.concave_vertex_count != 0) {
			final Node previous_v = p_vertices.get(computePreviousIndex(
					p_vertices, p_eartip_i));
			final Node current_v = p_vertices.get(p_eartip_i);
			final Node next_v = p_vertices.get(computeNextIndex(p_vertices,
					p_eartip_i));

			if (isAnyVertexInTriangle(p_vertices, p_vertex_types, previous_v.x,
					previous_v.y, current_v.x, current_v.y, next_v.x, next_v.y)) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	private void cutEarTip(final ArrayList<Node> p_vertices,
			final int p_eartip_i, final ArrayList<Node> p_triangles) {
		final int previous_i = computePreviousIndex(p_vertices, p_eartip_i);
		final int next_i = computeNextIndex(p_vertices, p_eartip_i);

		if (!isCollinear(p_vertices, previous_i, p_eartip_i, next_i)) {
			p_triangles.add(new Node(p_vertices.get(previous_i)));
			p_triangles.add(new Node(p_vertices.get(p_eartip_i)));
			p_triangles.add(new Node(p_vertices.get(next_i)));
		}

		p_vertices.remove(p_eartip_i);
		if (p_vertices.size() >= 3) {
			removeCollinearNeighborEarsAfterRemovingEarTip(p_vertices,
					p_eartip_i);
		}
	}

	private static void removeCollinearNeighborEarsAfterRemovingEarTip(
			final ArrayList<Node> p_vertices, final int p_eartip_i) {
		final int co_checknext_i = p_eartip_i % p_vertices.size();
		int co_checkprev_i = computePreviousIndex(p_vertices, co_checknext_i);

		if (isCollinear(p_vertices, co_checknext_i)) {
			p_vertices.remove(co_checknext_i);

			if (p_vertices.size() > 3) {
				co_checkprev_i = computePreviousIndex(p_vertices,
						co_checknext_i);
				if (isCollinear(p_vertices, co_checkprev_i)) {
					p_vertices.remove(co_checkprev_i);
				}
			}
		} else if (isCollinear(p_vertices, co_checkprev_i)) {
			p_vertices.remove(co_checkprev_i);
		}
	}

	private static boolean isCollinear(final ArrayList<Node> p_vertices,
			final int p_i) {
		final int previousIndex = computePreviousIndex(p_vertices, p_i);
		final int nextIndex = computeNextIndex(p_vertices, p_i);

		return isCollinear(p_vertices, previousIndex, p_i, nextIndex);
	}

	private static boolean isCollinear(final ArrayList<Node> p_vertices,
			final int p_prev_i, final int p_i, final int p_next_i) {
		final Node prev_v = p_vertices.get(p_prev_i);
		final Node vertex = p_vertices.get(p_i);
		final Node next_v = p_vertices.get(p_next_i);

		return computeSpannedAreaSign(prev_v.x, prev_v.y, vertex.x, vertex.y,
				next_v.x, next_v.y) == 0;
	}

	private static int computePreviousIndex(final List<Node> p_vertices,
			final int p_i) {
		return p_i == 0 ? p_vertices.size() - 1 : p_i - 1;
	}

	private static int computeNextIndex(final List<Node> p_vertices,
			final int p_i) {
		return p_i == p_vertices.size() - 1 ? 0 : p_i + 1;
	}
}