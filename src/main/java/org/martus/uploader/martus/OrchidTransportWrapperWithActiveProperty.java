package org.martus.uploader.martus;

import org.martus.common.network.MartusOrchidDirectoryStore;
import org.martus.common.network.OrchidTransportWrapper;

public class OrchidTransportWrapperWithActiveProperty extends OrchidTransportWrapper
{
	public static OrchidTransportWrapperWithActiveProperty createWithoutPersistentStore() throws Exception
	{
		return create(new MartusOrchidDirectoryStore());
	}
	
	public static OrchidTransportWrapperWithActiveProperty create(MartusOrchidDirectoryStore storeToUse) throws Exception
	{
		return new OrchidTransportWrapperWithActiveProperty(storeToUse);
	}
	
	protected OrchidTransportWrapperWithActiveProperty(MartusOrchidDirectoryStore storeToUse) throws Exception
	{
		super(storeToUse);
	}
	
	@Override
	public boolean isTorEnabled()
	{
		return false;
	}
}