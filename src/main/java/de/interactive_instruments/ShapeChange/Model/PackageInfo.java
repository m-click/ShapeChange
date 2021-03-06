/**
 * ShapeChange - processing application schemas for geographic information
 *
 * This file is part of ShapeChange. ShapeChange takes a ISO 19109 
 * Application Schema from a UML model and translates it into a 
 * GML Application Schema or other implementation representations.
 *
 * Additional information about the software can be found at
 * http://shapechange.net/
 *
 * (c) 2002-2014 interactive instruments GmbH, Bonn, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact:
 * interactive instruments GmbH
 * Trierer Strasse 70-72
 * 53115 Bonn
 * Germany
 */

package de.interactive_instruments.ShapeChange.Model;

import java.util.List;
import java.util.SortedSet;

public interface PackageInfo extends Info {

	/**
	 * Determine the targetNamespace of the GML applications schema to be
	 * generated. The item is used from the configuration or - if not present
	 * there - from the tagged value either on this package or one of its
	 * ancestors.
	 */
	public String targetNamespace();

	/**
	 * Determine the namespace abbreviation of the GML applications schema to be
	 * generated. The item is used from the configuration or - if not present
	 * there - from the tagged value either on this package or one of its
	 * ancestors.
	 */
	public String xmlns();

	public String xsdDocument();

	public String gmlProfileSchema();

	/**
	 * Determine the version attribute to be applied to the GML application
	 * schema. It is taken either from the configuration or from a tagged value
	 * on this package or any of its ancestors.
	 */
	public String version();

	public PackageInfo owner();

	public String schemaId();

	public PackageInfo rootPackage();

	/**
	 * Determine whether the package represents an 'application schema'. The
	 * package is regarded an 'application schema', if it carries a stereotype
	 * with normalized name "application schema".
	 */
	public boolean isAppSchema();

	/**
	 * Determine whether the package represents a schema. A package is assumed
	 * to represent a schema, if it contains a tagged value defining a
	 * "targetNamespace" (NOTE: there is a subtle difference to the package
	 * 'having' a targetNamespace, because the {@link #targetNamespace()} method
	 * may retrieve the targetNamespace of a package from one of its ancestors).
	 * It is also regarded a schema, if the package is named in a PackageInfo
	 * entry of the Configuration document.
	 */
	public boolean isSchema();

	/**
	 * @return a set of directly contained (child) packages (shallow copy, NOT
	 *         deep copy). One or more of these packages may belong to a
	 *         different schema / targetNamespace.
	 */
	public SortedSet<PackageInfo> containedPackages();

	/**
	 * @return the set of classes that are directly contained in this package
	 *         (thus excluding classes from subpackages); can be empty but not
	 *         <code>null</code>
	 */
	public SortedSet<ClassInfo> containedClasses();

	public SortedSet<String> supplierIds();

	/**
	 * @return metadata about the diagrams relevant for this package,
	 *         <code>null</code> if no diagrams are available
	 */
	public List<ImageMetadata> getDiagrams();

	/**
	 * @param diagrams
	 *            metadata about the diagrams relevant for this class
	 */
	public void setDiagrams(List<ImageMetadata> diagrams);
};
