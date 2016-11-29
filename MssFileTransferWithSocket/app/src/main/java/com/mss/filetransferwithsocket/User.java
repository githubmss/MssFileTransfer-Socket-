package com.mss.filetransferwithsocket;


/**
 * A data structure to hold information about a discovered peer
 */
public class User
{
	
	String mUniqueID;
	String mIPAddr;
	String name;

	public User(String uniqueID, String IPaddr, String name)
	{
		this.mUniqueID = uniqueID;
		this.mIPAddr = IPaddr;
		this.name = name;
	}
	
	@Override
	public boolean equals(Object other_)
	{
		if(other_==null || !( other_ instanceof User) ){return false;}
		User other = (User)other_;
		
		if(mUniqueID ==null && mIPAddr ==null && name==null && other.mUniqueID ==null && other.mIPAddr ==null && other.name==null){return true;}
		
		if(other.mUniqueID ==null && mUniqueID !=null){return false;}
		if(mUniqueID ==null && other.mUniqueID !=null){return false;}
		
		if(other.mIPAddr ==null && mIPAddr !=null){return false;}
		if(mIPAddr ==null && other.mIPAddr !=null){return false;}
		
		if(other.name==null && name!=null){return false;}
		if(name==null && other.name!=null){return false;}
		
		
		if (other.mUniqueID !=null && !other.mUniqueID.equalsIgnoreCase(this.mUniqueID))
			return false;
		if (other.mIPAddr !=null && !other.mIPAddr.equalsIgnoreCase(this.mIPAddr))
			return false;
		if (other.name!=null && !other.name.equalsIgnoreCase(this.name))
			return false;
		
		return true;
	}
}//class
