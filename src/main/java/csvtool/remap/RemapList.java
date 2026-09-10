package csvtool.remap;

import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record RemapList(List<CSVRemap> list)
{
	private static final LogWrapper LOGGER = new LogWrapper(RemapList.class);

	public RemapList()
	{
		this(null);
	}

	public RemapList(@Nullable List<CSVRemap> list)
	{
		if (list == null || list.isEmpty())
		{
			this.list = new ArrayList<>();
		}
		else
		{
			this.list = new ArrayList<>(list);
		}
	}

	public int size()
	{
		return this.list.size();
	}

	public boolean isEmpty()
	{
		return this.list.isEmpty();
	}

	public RemapList addRemap(@Nonnull CSVRemap remap)
	{
		if (remap.getParams() == null)
		{
			remap = remap.setParams(List.of());
		}

		this.list.add(remap);
		return this;
	}

	public boolean hasRemap(int entry)
	{
		if (entry > this.list.size())
		{
			return false;
		}

		return this.list.get(entry) != null;
	}

	public @Nullable CSVRemap getRemap(int entry)
	{
		if (entry > this.list.size())
		{
			LOGGER.error("setRemap(): Error; Entry: [{}] > size [{}]", entry, this.list.size());
			return null;
		}

		return this.list.get(entry);
	}

	public void setRemap(int entry, @Nonnull CSVRemap newRemap)
	{
		if (entry > this.list.size())
		{
			LOGGER.error("setRemap(): Error; Entry: [{}] > size [{}]", entry, this.list.size());
			return;
		}

		this.list.set(entry, newRemap);
	}

	public void clear()
	{
		if (!this.list.isEmpty())
		{
			this.list.forEach(CSVRemap::clear);
		}
	}

	@Override
	public @Nonnull String toString()
	{
		StringBuilder builder = new StringBuilder("CSVRemapList[");

		if (!this.list.isEmpty())
		{
			for (int i = 0; i < this.list.size(); i++)
			{
				builder.append(i);

				if (i == 0)
				{
					builder.append("{");
				}
				else
				{
					builder.append(",{");
				}

				builder.append(this.list.get(i).toString()).append("}");
			}
		}

		builder.append("]");
		return builder.toString();
	}
}
