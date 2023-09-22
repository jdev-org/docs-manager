package org.georchestra.docsmanager.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Matcher;

public final class RoleHelper {
    @Value("${docs.roles.reader}")
    List<String> additionnalReaders;

    /**
     * Will get id plugin from role
     * getIdFromRole("SV_ABCD_READ") => "ABCD"
     * 
     * @param role to compare
     * @return plugin id from role
     */
    public static String getIdFromRole(String role) {
        String regex = "_(.*?)_";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(role);
        String idPlugin = "";
        if (matcher.find()) {
            idPlugin = matcher.group(1);
        }
        return idPlugin;
    }

    /**
     * Will filter user roles by limited roles.
     * filterRolesByRoles(["a","b"], ["a","c"]) => ["a"]
     * 
     * @param roles        user roles value
     * @param rolesToMatch roles attempt
     * @return user roles match
     */
    public static List<String> filterRolesByRoles(String roles, List<String> rolesToMatch) {
        List<String> rolesList = Arrays.asList(roles.split(";"));
        List<String> filteredList = rolesList.stream()
                .filter(r -> rolesToMatch.contains(r))
                .collect(Collectors.toList());
        return filteredList;
    }

    /**
     * Return a list of authorized documents readers.
     * readerRoles("CARTEAUX", ["ROLE_MAPSTORE_ADMIN"]) =>
     * ["ROLE_SV_CARTEAUX_READ", "ROLE_MAPSTORE_ADMIN"]
     * 
     * @param idPlugin   plugin id uppercase
     * @param adminRoles from config
     * @return readers roles list
     */
    public static List<String> readerRoles(
            String idPlugin,
            List<String> adminRoles) {
        String readerRole = idPlugin + "_READ";
        String svReaderRole = "SV_" + idPlugin + "_READ";
        String roleSvReaderRole = "ROLE_SV" + idPlugin + "_READ";
        List<String> defaultReaders = writerRoles(idPlugin, adminRoles);
        defaultReaders.add(readerRole);
        defaultReaders.add(svReaderRole);
        defaultReaders.add(roleSvReaderRole);
        return defaultReaders;
    }

    /**
     * Return a list of authorized documents writers.
     * writerRoles("CARTEAUX", ["ROLE_MAPSTORE_ADMIN"]) =>
     * ["SV_CARTEAUX_EDIT", "ROLE_MAPSTORE_ADMIN"]
     * 
     * @param idPlugin   plugin id uppercase
     * @param adminRoles admin roles
     * @return writers roles list
     */
    public static List<String> writerRoles(
            String idPlugin,
            List<String> defaultRoles) {
        String writerRole = idPlugin + "_EDIT";
        String svWriterRole = "SV_" + writerRole;
        String roleSvWriterRole = "ROLE_SV_" + writerRole;
        defaultRoles.add(writerRole);
        defaultRoles.add(svWriterRole);
        defaultRoles.add(roleSvWriterRole);
        return defaultRoles;
    }

    /**
     * 
     * @param plugin       plugin id uppercase
     * @param role         current user roles
     * @param defaultRoles addditional for reader or editor, concat with admin roles
     * @return true if reader
     */
    public static Boolean isReader(String plugin, String role, List<String> defaultRoles) {
        String idPlugin = plugin.toUpperCase();
        List<String> readers = RoleHelper.readerRoles(idPlugin, defaultRoles);
        List<String> authorizedRoles = RoleHelper.filterRolesByRoles(role, readers);
        return authorizedRoles.size() > 0;
    }

    /**
     * 
     * @param plugin       plugin id uppercase
     * @param role         current user roles
     * @param defaultRoles addditional for reader or editor, concat with admin roles
     * @return true if writer
     */
    public static Boolean isWriter(String plugin, String role, List<String> defaultRoles) {
        String idPlugin = plugin.toUpperCase();
        List<String> writers = RoleHelper.writerRoles(idPlugin, defaultRoles);
        List<String> authorizedRoles = RoleHelper.filterRolesByRoles(role, writers);
        return authorizedRoles.size() > 0;
    }

    /**
     * Return true if user is an admin.
     * 
     * @param role       current user roles
     * @param adminRoles list from application properties
     * @return true if admin
     */
    public static Boolean isAdmin(String role, List<String> adminRoles) {
        List<String> authorizedRoles = RoleHelper.filterRolesByRoles(role, adminRoles);
        return authorizedRoles.size() > 0;
    }

    /**
     * 
     * @param type            to find key in property => values : editor | reader
     * @param additionalRoles additional roles from application properties
     * @param plugin          plugin id uppercase
     * @return additional roles as list
     */
    public static List<String> getAdditionalRoles(String type, String additionalRoles, String plugin) {
        JSONObject roles = new JSONObject(additionalRoles);
        String idPlugin = plugin.toUpperCase();
        if (!roles.has(idPlugin)) {
            return new ArrayList<String>();
        }
        JSONObject additionalRolesByPluginId = roles.getJSONObject(idPlugin);
        List<Object> rolesFromObject = additionalRolesByPluginId.getJSONArray(type).toList();
        return rolesFromObject.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    /**
     * Get list of readers or editors from config.
     * Will concat admin and additional roles.
     * 
     * @param plugin          plugin id uppercase
     * @param type            to find key in property => values : editor | reader
     * @param adminRoles      admin roles
     * @param additionalRoles additional roles from application properties
     * @return full list
     */
    public static List<String> getFullAuthorizedRoles(String plugin, String type, List<String> adminRoles,
            String additionalRoles) {
        List<String> defaultReadersRoles = RoleHelper.getAdditionalRoles(type, additionalRoles, plugin);
        return Stream.concat(defaultReadersRoles.stream(), adminRoles.stream()).collect(Collectors.toList());
    }
}
