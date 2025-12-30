package org.odiem.test.pojo;

import org.odiem.sdk.annotations.Child;
import org.odiem.sdk.annotations.ObjectClass;

@ObjectClass("inetOrgPerson")
public class OdmPerson extends OdmAbstractPerson {

	@Child
	private OdmFriend[] friends;

	public void setFriends(OdmFriend... friends) {
		this.friends = friends;
	}

	public OdmFriend[] getFriends() {
		return friends;
	}

}
