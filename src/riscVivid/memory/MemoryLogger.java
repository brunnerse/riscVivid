package riscVivid.memory;

import java.util.ArrayList;

import riscVivid.datatypes.uint32;

public class MemoryLogger {
	private ArrayList<DataSegment> segments;

	public MemoryLogger() {
	    this.segments = new ArrayList<DataSegment>();
	}
	
	public MemoryLogger(MemoryLogger ml) {
	    this.segments = new ArrayList<DataSegment>(ml.segments);
	}
	/**
	 * init segments from a string "segment1.begin-segment1.end,segment2.begin-segment2.end"
	 */
	public void initFromCfgString(String cfg) {
		segments.clear();
		String[] segmentsStr = cfg.split(",");
		for (String segStr : segmentsStr) {
			String[] startEndStr = segStr.split("-");
			if (startEndStr.length == 2)
				segments.add(new DataSegment(Integer.parseInt(startEndStr[0]),
						Integer.parseInt(startEndStr[1])));
		}
	}

	/**
	 * @return a string "segment1.begin-segment1.end, segment2.begin-segment2.end"
	 */
	public String toCfgString() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < segments.size(); ++i) {
			DataSegment seg = segments.get(i);
			s.append("0x"+Integer.toHexString(seg.getStart()) + "-0x" + Integer.toHexString(seg.getEnd()));
			if (i < segments.size() - 1)
				s.append(", ");
		}
		return s.toString();
	}
	
	public boolean isEmpty() {
	    return segments.isEmpty();
	}

	/**
	 * @param startAddress is inclusive
	 * @param endAddress is exclusive
	 */
	public void add(int startAddress, int endAddress) {
		if (startAddress >= endAddress) // segment is empty
			return;
		if (segments.size() == 0) {
			segments.add(new DataSegment(startAddress, endAddress));
		} else {
			int idx = findSubsequentSegment(startAddress);
			DataSegment prev = null;
			boolean appended = false;
			// if previous DataSegment exists, check if new segment can be appended to previous segment
			if (idx - 1 >= 0) {
				prev = segments.get(idx-1);   //prev.start <= startAddress
				if (startAddress <= prev.end) {
					if (endAddress <= prev.end) {
						// prev fully contains the new DataSegment, nothing to do
					    return;
					} else {
						// prev.end < endAddress: merge previous segment with new segment
						prev.setEnd(endAddress);
					}
					appended = true;
				}
			}
			// if subsequent DataSegment exists, check if new segment can be appended to subsequent segment
			if (idx < segments.size()) {
				DataSegment subsequent = segments.get(idx);
				// if subsequent segment is adjacent or overlapping, merge the segments
				if (endAddress >= subsequent.start) {
					subsequent.setStart(startAddress);
					if (endAddress > subsequent.getEnd())
						subsequent.setEnd(endAddress);
					// Test if previous segment can be merged with the subsequent segment
					if (prev != null) {
						if (prev.end >= subsequent.start) {
							// prev.getEnd() <= next.getEnd()) is true here
							prev.setEnd(subsequent.end);
							segments.remove(subsequent);
						}
					}
					appended = true;
				}
			}
			// if Segment couldn't be appended to any other segment: insert a new segment
			if (!appended) {
				segments.add(idx, new DataSegment(startAddress, endAddress));
			}
		}
	}
	
	   public void addBytes(uint32 address, int numBytes) {
	        add(address.getValue(), address.getValue() + numBytes);
	    }

	/**
	 * @param startAddress is inclusive
	 * @param endAddress is exclusive
	 * @return if specified memory segment is in the log 
	 */
	public boolean check(int startAddress, int endAddress) {
		int idx = findSubsequentSegment(startAddress);
		idx -= 1;
	    // idx is now always < segments.size()
		// test if segments.get(idx) exists; in that case, test if it contains the segment
		if (idx >= 0) {
			if (segments.get(idx).getStart() <= startAddress && 
					segments.get(idx).getEnd() >= endAddress)
				return true;
		}
		return false;
	}
	
	public boolean checkBytes(uint32 address, int numBytes) {
		return check(address.getValue(), address.getValue() + numBytes);
	}

	/**
	 * @return the idx with address < segment[idx].start and address >= segment[i-1].start;
	 *  if no such segments exists, it returns segments.size()
	 *  if no segments exist, it returns -1
	 */
	private int findSubsequentSegment(int address) {
		int upper = segments.size() - 1, lower = 0;
		if (segments.size() == 0)
			return -1;
		// should always terminate
		while (true) {
			int idx = (upper + lower) / 2;

			if (address >= segments.get(idx).getStart()) {
				lower = idx+1;
				// if last element reached and it is still smaller than the address:
				if (lower >= segments.size())
					return segments.size();
			} else { // (address < segments.get(idx).getStart())
				upper = idx - 1;
				// if first element reached or nextlower segment is lower than address
				if (idx == 0 || address >= segments.get(idx - 1).getStart())
					return idx;
			}
		}
	}

	@Override
	public String toString() {
		return toCfgString();
	}

	private static class DataSegment {
		// DataSegment include the start address and the end address and all addresses inbetween.
		private int start, end;
		public DataSegment(int start, int end) {
			if (end < start)
				throw new RuntimeException("start must be lower or equal to end");
			this.start = start;
			this.end = end;
		}

		public int getStart() {
			return start;
		}
		public void setStart(int start) {
			if (end < start)
				throw new RuntimeException("start must be lower or equal to end");
			this.start = start;
		}
		public int getEnd() {
			return end;
		}
		public void setEnd(int end) {
			if (end < start)
				throw new RuntimeException("end must be bigger or equal to start");
			this.end = end;
		}

		public boolean contains(int address) {
			return start <= address && address <= end;
		}
	}
}
