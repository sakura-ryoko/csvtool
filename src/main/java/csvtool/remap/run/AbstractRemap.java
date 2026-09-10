package csvtool.remap.run;

import csvtool.remap.RemapResult;
import csvtool.remap.RemapType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRemap
{
	private final RemapType type;
	private String dataIn;
	private List<String> rowIn;

	public AbstractRemap(@Nonnull RemapType type)
	{
		this.type = type;
		this.dataIn = "";
		this.rowIn = new ArrayList<>();
	}

	public RemapType type()
	{
		return this.type;
	}

	public String dataIn()
	{
		return this.dataIn;
	}

	public List<String> rowIn()
	{
		return this.rowIn;
	}

	protected void setDataIn(@Nonnull String dataIn)
	{
		if (!dataIn.isEmpty())
		{
			this.dataIn = dataIn;
		}
	}

	protected void setRowIn(@Nonnull List<String> rowIn)
	{
		if (!rowIn.isEmpty())
		{
			this.rowIn = rowIn;
		}
	}

	public abstract RemapResult runRemap(@Nonnull String dataIn, @Nonnull List<String> rowIn);
}
