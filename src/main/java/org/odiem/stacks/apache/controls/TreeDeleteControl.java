package org.odiem.stacks.apache.controls;

import org.apache.directory.shared.ldap.model.message.controls.AbstractControl;

public class TreeDeleteControl extends AbstractControl {

	private static final long serialVersionUID = -9145321436984993695L;
	
	/**
	 * The control OID for the transaction specification control.
	 */
	public static final String OID_TREE_DELETE_CONTROL = "1.2.840.113556.1.4.805";

	public TreeDeleteControl() {
		super(OID_TREE_DELETE_CONTROL, true);
	}

}