package factionsystem.Objects;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;

import factionsystem.Main;
import factionsystem.Subsystems.ConfigSubsystem;

public class Gate {

	private String name = "gateName";
	private boolean open = false;	
	private boolean vertical = true;
	private GateCoord coord1 = null;
	private GateCoord coord2 = null;
	private GateCoord trigger = null;
	private Material material = Material.IRON_BARS;
	private World world = null;
	
	private Sound soundEffect = Sound.BLOCK_ANVIL_HIT;
	
	private enum GateStatus { READY, OPENING, CLOSING };
	private GateStatus gateStatus = GateStatus.READY;
	
	public boolean isIntersecting(Gate gate)
	{
		boolean xoverlap = coord2.getX() > gate.coord1.getX() && coord1.getX() < coord2.getX();
		boolean yoverlap = coord2.getY() > gate.coord1.getY() && coord1.getY() < gate.coord1.getY();
		boolean zoverlap = coord2.getZ() > gate.coord1.getZ() && coord1.getZ() < coord2.getZ();
		return xoverlap && yoverlap && zoverlap;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String value)
	{
		name = value;
	}
	
	public boolean isOpen()
	{
		return open ? true : false;
	}
	
	public boolean isClosed()
	{
		return open ? false : true;
	}
	
	public GateCoord getTrigger()
	{
		return trigger;
	}
	
	public GateCoord getCoord1()
	{
		return coord1;
	}
	
	public GateCoord getCoord2()
	{
		return coord2;
	}
	
	private Main main;
	
	public Gate(Main plugin)
	{
		main = plugin;
	}
	
	public boolean isParallelToZ()
	{
		if (coord1 != null && coord2 != null)
		{
			if (coord1.getZ() != coord2.getZ())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	public boolean isParallelToX()
	{
		if (coord1 != null && coord2 != null)
		{
			if (coord1.getX() != coord2.getX())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	public ArrayList<Block> GateBlocks()
	{
		ArrayList<Block> blocks = new ArrayList<Block>();
		for (int y = coord1.getY(); y < coord2.getY(); y++)
		{
			for (int z = coord1.getZ(); z < coord2.getZ(); z++)
			{
				for (int x = coord1.getX(); x < coord2.getX(); x++)
				{
					blocks.add(world.getBlockAt(x, y, z));
				}
			}
		}
		return blocks;
	}
	
	public boolean gateBlocksMatch(Material mat)
	{
		int topY = coord1.getY();
		int bottomY = coord2.getY();
		if (coord2.getY() > coord1.getY())
		{
			topY = coord2.getY();
			bottomY = coord1.getY();
		}
		
		int leftX = coord1.getX();
		int rightX = coord2.getX();
		if (coord2.getX() < coord1.getX())
		{
			leftX = coord2.getX();
			rightX = coord1.getX();
		}

		int leftZ = coord1.getZ();
		int rightZ = coord2.getZ();
		if (coord2.getZ() < coord1.getZ())
		{
			leftZ = coord2.getZ();
			rightZ = coord1.getZ();
		}
		
		if (isParallelToZ())
		{
			rightX++;
		}
		else if (isParallelToX())
		{
			rightZ++;
		}
		
		for (int y = topY; y > bottomY; y--)
		{
			for (int z = leftZ; z < rightZ; z++)
			{
				for (int x = leftX; x < rightX; x++)
				{
					if (!world.getBlockAt(x, y, z).getType().equals(mat))
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public boolean AddCoord(Block clickedBlock)
	{
		if (coord1 == null)
		{
			world = clickedBlock.getWorld();
			coord1 = new GateCoord(clickedBlock);
			material = clickedBlock.getType(); 
		}
		else if (coord2 == null)
		{
			if (!coord1.getWorld().equalsIgnoreCase(clickedBlock.getWorld().getName()))
			{
				return false;
			}
			if (!clickedBlock.getType().equals(material))
			{
				return false;
			}		
			// GetDim methods use coord2 object.
			coord2 = new GateCoord(clickedBlock);
			if (GetDimX() > 1 && GetDimY() > 1 && GetDimZ() > 1)
			{
				// No cuboids.
				coord2 = null;
				return false;
			}

			if (isParallelToX() && GetDimY() > 1)
			{
				vertical = true;
			}
			else if (isParallelToZ() && GetDimY() > 1)
			{
				vertical = true;
			}
			else
			{
				vertical = false;
			}
			
			int area = 0;
			if (vertical)
			{
				if (isParallelToX())
				{
					area = GetDimX() * GetDimY();
				}
				else if (isParallelToZ())
				{
					area = GetDimZ() * GetDimY();
				}
			}
			else if (!vertical)
			{
				if (isParallelToX())
				{
					area = GetDimX() * GetDimY();
				}
				else if (isParallelToZ())
				{
					area = GetDimZ() * GetDimY();
				}
			}
			if (area > main.getConfig().getInt("factionMaxGateArea"))
			{
				// Gate size exceeds config limit.
				coord2 = null;
				return false;
			}
			if (!gateBlocksMatch(material))
			{
				coord2 = null;
				return false;
			}
		}
		else
		{
			trigger = new GateCoord(clickedBlock);
		}
		return true;
	}
	
	public int GetDimX()
	{
		return GetDimX(coord1, coord2);
	}
	public int GetDimY()
	{
		return GetDimY(coord1, coord2);
	}
	public int GetDimZ()
	{
		return GetDimZ(coord1, coord2);
	}
	
	public int GetDimX(GateCoord first, GateCoord second)
	{
		GateCoord tmp;
		if (first.getX() > second.getX())
		{
			tmp = second;
			second = first;
			first = tmp;
		}
		return second.getX() - first.getX();
	}
	public int GetDimY(GateCoord first, GateCoord second)
	{
		GateCoord tmp;
		if (first.getY() > second.getY())
		{
			tmp = second;
			second = first;
			first = tmp;
		}
		return second.getY() - first.getY();
	}
	public int GetDimZ(GateCoord first, GateCoord second)
	{
		GateCoord tmp;
		if (first.getZ() > second.getZ())
		{
			tmp = second;
			second = first;
			first = tmp;
		}
		return second.getZ() - first.getZ();
	}
	
	public void OpenGate()
	{
		if (open || !gateStatus.equals(GateStatus.READY))
			return;
		open = true;
		gateStatus = GateStatus.OPENING;
		// For vertical we only need to iterate over x/y
		if (vertical)
		{
			if (isParallelToX())
			{
				int topY = coord1.getY();
				int _bottomY = coord2.getY();
				if (coord2.getY() > coord1.getY())
				{
					topY = coord2.getY();
					_bottomY = coord1.getY();
				}
				final int bottomY = _bottomY;
				
				int _leftX = coord1.getX();
				int _rightX = coord2.getX();
				if (coord2.getX() < coord1.getX())
				{
					_leftX = coord2.getX();
					_rightX = coord1.getX();
				}
	
				final int leftX = _leftX;
				final int rightX = _rightX;
				
				int c = 0;
				for (int y = bottomY; y <= topY; y++)
				{
					c++;
					final int blockY = y;
					Bukkit.getScheduler().runTaskLater(main, new Runnable() {
						Block b;
	                    @Override
	                    public void run() {
	        				for (int x = leftX; x <= rightX; x++)
	        				{
	        					b = world.getBlockAt(x, blockY, coord1.getZ());
	        					b.setType(Material.AIR);
	        					world.playSound(b.getLocation(), soundEffect, 0.1f, 0.1f);
	        				}
	        				if (blockY == bottomY + 1)
	        				{
	        					gateStatus = GateStatus.READY;
	        				}
	                    }
	                }, c * 10);
				}
			}
			else if (isParallelToZ())
			{
				int topY = coord1.getY();
				int _bottomY = coord2.getY();
				if (coord2.getY() > coord1.getY())
				{
					topY = coord2.getY();
					_bottomY = coord1.getY();
				}
				final int bottomY = _bottomY;
				int _leftZ = coord1.getZ();
				int _rightZ = coord2.getZ();
				if (coord2.getZ() < coord1.getZ())
				{
					_leftZ = coord2.getZ();
					_rightZ = coord1.getZ();
				}
	
				final int leftZ = _leftZ;
				final int rightZ = _rightZ;
				
				int c = 0;
				for (int y = bottomY; y <= topY; y++)
				{
					c++;
					final int blockY = y;
					Bukkit.getScheduler().runTaskLater(main, new Runnable() {
						Block b;
	                    @Override
	                    public void run() {
	        				for (int z = leftZ; z <= rightZ; z++)
	        				{
	        					b = world.getBlockAt(coord1.getX(), blockY, z);
	        					b.setType(Material.AIR);
	        					world.playSound(b.getLocation(), soundEffect, 0.1f, 0.1f);
	        				}
	        				if (blockY == bottomY + 1)
	        				{
	        					gateStatus = GateStatus.READY;
	        				}
	                    }
	                }, c * 10);
				}			
			}
			
		}
		else
		{
			// TODO: Bridge code iterates over x/z
		}
	}
	
	public void CloseGate()
	{

		if (!open || !gateStatus.equals(GateStatus.READY))
			return;
		
		open = false;
		gateStatus = GateStatus.CLOSING;
		// For vertical we only need to iterate over x/y
		if (vertical)
		{
			if (isParallelToX())
			{
				int topY = coord1.getY();
				int _bottomY = coord2.getY();
				if (coord2.getY() > coord1.getY())
				{
					topY = coord2.getY();
					_bottomY = coord1.getY();
				}
				final int bottomY = _bottomY;
				int _leftX = coord1.getX();
				int _rightX = coord2.getX();
				if (coord2.getX() < coord1.getX())
				{
					_leftX = coord2.getX();
					_rightX = coord1.getX();
				}
	
				final int leftX = _leftX;
				final int rightX = _rightX;
				
				int c = 0;
				for (int y = topY; y >= bottomY; y--)
				{
					c++;
					final int blockY = y;
					Bukkit.getScheduler().runTaskLater(main, new Runnable() {
						Block b;
	                    @Override
	                    public void run() {
	        				for (int x = leftX; x <= rightX; x++)
	        				{
	        					b = world.getBlockAt(x, blockY, coord1.getZ());
	        					b.setType(material);
	        					world.playSound(b.getLocation(), soundEffect, 0.1f, 0.1f);
	        				}
	        				if (blockY == bottomY + 1)
	        				{
	        					gateStatus = GateStatus.READY;
	        				}
	                    }
	                }, c * 10);
				}
			}
			else if (isParallelToZ())
			{
				int topY = coord1.getY();
				int _bottomY = coord2.getY();
				if (coord2.getY() > coord1.getY())
				{
					topY = coord2.getY();
					_bottomY = coord1.getY();
				}
				final int bottomY = _bottomY;
				int _leftZ = coord1.getZ();
				int _rightZ = coord2.getZ();
	
				if (coord2.getZ() < coord1.getZ())
				{
					_leftZ = coord2.getZ();
					_rightZ = coord1.getZ();
				}
				final int leftZ = _leftZ;
				final int rightZ = _rightZ;
				
				int c = 0;
				for (int y = topY; y >= bottomY; y--)
				{
					c++;
					final int blockY = y;
					Bukkit.getScheduler().runTaskLater(main, new Runnable() {
						Block b;
	                    @Override
	                    public void run() {
	        				for (int z = leftZ; z <= rightZ; z++)
	        				{
	        					b = world.getBlockAt(coord1.getX(), blockY, z);
	        					b.setType(material);
	        					world.playSound(b.getLocation(), soundEffect, 0.1f, 0.1f);
	        				}
	        				if (blockY == bottomY + 1)
	        				{
	        					gateStatus = GateStatus.READY;
	        				}
	                    }
	                }, c * 10);
				}			
			}
		}
		else
		{
			// TODO: Bridge code iterates over x/z
		}
	}
	
	public void FillGate()
	{

		if (!open)
			return;
		
		open = false;
		// For vertical we only need to iterate over x/y
		if (vertical)
		{
			if (isParallelToX())
			{
				int topY = coord1.getY();
				int bottomY = coord2.getY();
				if (coord2.getY() > coord1.getY())
				{
					topY = coord2.getY();
					bottomY = coord1.getY();
				}
				
				int _leftX = coord1.getX();
				int _rightX = coord2.getX();
				if (coord2.getX() < coord1.getX())
				{
					_leftX = coord2.getX();
					_rightX = coord1.getX();
				}
	
				final int leftX = _leftX;
				final int rightX = _rightX;
				
				for (int y = topY; y >= bottomY; y--)
				{
					Block b = null;
    				for (int x = leftX; x <= rightX; x++)
    				{
    					b = world.getBlockAt(x, y, coord1.getZ());
    					b.setType(material);
    				};
					if (b != null)
						world.playSound(b.getLocation(), soundEffect, 0.1f, 0.1f);
				}
			}
			else if (isParallelToZ())
			{
				int topY = coord1.getY();
				int bottomY = coord2.getY();
				if (coord2.getY() > coord1.getY())
				{
					topY = coord2.getY();
					bottomY = coord1.getY();
				}
				
				int _leftZ = coord1.getZ();
				int _rightZ = coord2.getZ();
	
				if (coord2.getZ() < coord1.getZ())
				{
					_leftZ = coord2.getZ();
					_rightZ = coord1.getZ();
				}
				final int leftZ = _leftZ;
				final int rightZ = _rightZ;
				
				for (int y = topY; y >= bottomY; y--)
				{
					Block b = null;
    				for (int z = leftZ; z <= rightZ; z++)
    				{
    					b = world.getBlockAt(coord1.getX(), y, z);
    					b.setType(material);
    				};
					if (b != null)
						world.playSound(b.getLocation(), soundEffect, 0.1f, 0.1f);
				}		
			}
		}
		else
		{
			// TODO: Bridge code iterates over x/z
		}
	}
	
	public boolean hasBlock(Block targetBlock)
	{
		if (targetBlock.getX() >= coord1.getX() && targetBlock.getX() <= coord2.getX()
				&& targetBlock.getY() >= coord1.getY() && targetBlock.getY() <= coord2.getY()
				&& targetBlock.getZ() >= coord1.getZ() && targetBlock.getZ() <= coord2.getZ())
		{
			return true;
		}
		return false;
	}
	
	public String coordsToString()
	{
		return String.format("(%d, %d, %d - %d, %d, %d)", coord1.getX(), coord1.getY(), coord1.getZ(), coord2.getX(), coord2.getY(), coord2.getZ());
	}
}
