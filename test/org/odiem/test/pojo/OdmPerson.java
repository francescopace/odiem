package org.odiem.test.pojo;

import javax.xml.bind.annotation.XmlRootElement;

import org.odiem.sdk.annotations.Child;
import org.odiem.sdk.annotations.ObjectClass;

@XmlRootElement
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
