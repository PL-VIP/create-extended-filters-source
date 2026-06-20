package com.sure.createextendedfilters.filter;

import java.util.Set;

public interface FilterMenuExtension {
	Set<String> createExtendedFilters$getExcludedPaths();

	void createExtendedFilters$toggleExcludedPath(String exclusionPath);
}
