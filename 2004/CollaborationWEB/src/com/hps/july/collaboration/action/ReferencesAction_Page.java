package com.hps.july.collaboration.action;

import java.io.IOException;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.hps.july.collaboration.formbean.ReferencesForm_Page;

/**
 * @version 	1.0
 * @author Ildar
 */
public class ReferencesAction_Page extends Action {

	/**
	* Constructor
	*/
	public ReferencesAction_Page() {
		super();
	}
	public ActionForward perform(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response)
		throws IOException, ServletException {

		ActionErrors errors = new ActionErrors();
		ActionForward forward = new ActionForward();
		// return value
		ReferencesForm_Page referencesForm = (ReferencesForm_Page) form;
		referencesForm.doTask(errors);

		// If a message is required, save the specified key(s)
		// into the request for use by the <struts:errors> tag.
		if (!errors.empty()) {
			saveErrors(request, errors);
		}
		// Write logic determining how the user should be forwarded.
		forward = mapping.findForward(referencesForm.getComand());
		// Finish with
		return (forward);
	}
}
