package de.rherzog.master.thesis.slicer;

import com.ibm.wala.shrikeBT.MethodEditor;

public class SliceMethod {
	private MethodEditor methodEditor;
	private Integer featureLoggerVarIndex;

	public SliceMethod(MethodEditor methodEditor, Integer featureLoggerVarIndex) {
		setMethodEditor(methodEditor);
		setFeatureLoggerVarIndex(featureLoggerVarIndex);
	}

	public MethodEditor getMethodEditor() {
		return methodEditor;
	}

	public void setMethodEditor(MethodEditor methodEditor) {
		this.methodEditor = methodEditor;
	}

	public Integer getFeatureLoggerVarIndex() {
		return featureLoggerVarIndex;
	}

	public void setFeatureLoggerVarIndex(Integer featureLoggerVarIndex) {
		this.featureLoggerVarIndex = featureLoggerVarIndex;
	}
}
