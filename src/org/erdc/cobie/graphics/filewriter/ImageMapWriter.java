package org.erdc.cobie.graphics.filewriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bimserver.models.ifc2x3tc1.IfcSpace;
import org.erdc.cobie.graphics.COBieColor;
import org.erdc.cobie.graphics.EntitySetting;
import org.erdc.cobie.graphics.entities.Floor;
import org.erdc.cobie.graphics.entities.RenderableIfcProduct;
import org.erdc.cobie.graphics.entities.Space;
import org.erdc.cobie.graphics.settings.GlobalSettings;
import org.erdc.cobie.graphics.settings.OutputSettings;
import org.erdc.cobie.graphics.settings.OutputSettings.FileInfo;
import org.erdc.cobie.graphics.string.Default;
import org.erdc.cobie.graphics.string.Pattern;
import org.erdc.cobie.graphics2d.Triangle;
import org.erdc.cobie.shared.Common;
import org.erdc.cobie.shared.Zipper;
import org.erdc.cobie.shared.bimserver.COBieIfcUtility;
import org.erdc.cobie.shared.utility.StringUtils;

public class ImageMapWriter extends TemplateWriter
{
    private class SpaceWriter
    {
        private final Space space;
        private final String spaceID;
        private final String template;

        public SpaceWriter(Space space, String spaceID, String template)
        {
            this.space = space;
            this.spaceID = spaceID;
            this.template = template;
        }

        private String getAreaKey(int triangleNum)
        {
            return getSpaceID() + Default.KEY_SEPARATOR.toString() + Integer.toString(triangleNum);
        }

        public final Space getSpace()
        {
            return space;
        }
        
        public final String getSpaceID()
        {
            return spaceID;
        }

        public final String getTemplate()
        {
            return template;
        }

        private Map<String, Triangle> getTriangles()
        {
            Map<String, Triangle> triangles = new TreeMap<String, Triangle>();
            List<Triangle> rawTriangles = getSpace().for2D().getTransformedPolygon().toTriangles();
            
            for (int triangleNum = 0; triangleNum < rawTriangles.size(); triangleNum++)
            {
                triangles.put(getAreaKey(triangleNum + 1), rawTriangles.get(triangleNum));
            }

            return triangles;
        }

        public String write()
        {
            StringBuilder spaceHtml = new StringBuilder();

            spaceHtml.append(Default.SPACE_TAG.format(writeSpaceInfo(), writeAreas()));
            return spaceHtml.toString();
        }

        private String writeAreas()
        {
            Map<String, Triangle> triangles = getTriangles();
            String areas = Common.EMPTY_STRING.toString();

            for (String triangleID : triangles.keySet())
            {                
                String areaTemplate = getTemplate();
                areaTemplate = areaTemplate.replace(Pattern.ID.toString(), triangleID);
                areaTemplate = areaTemplate.replace(Pattern.CLASS.toString(), getSpace().getName());
                areaTemplate = areaTemplate.replace(Pattern.TITLE.toString(), Common.EMPTY_STRING.toString());//this.getSpace().getName());
                areaTemplate = areaTemplate.replace(Pattern.LINK.toString(), Default.HEX_COLOR_PREFIX.toString());
                areaTemplate = areaTemplate.replace(Pattern.COORDS.toString(), triangles.get(triangleID).toString());

                areas += areaTemplate;
            }

            return areas;
        }
        
        private String writeSpaceInfo()
        {
            IfcSpace ifcSpace = (IfcSpace)getSpace().getProduct();
                        
            String name = getSpace().getName();
            String description = ifcSpace.getDescription();
            String category = COBieIfcUtility.getObjectClassificationCategoryString(ifcSpace);
            
            StringBuilder infoBuilder = new StringBuilder();
            
            if (!StringUtils.isNullOrEmpty(name))
            {
                infoBuilder.append(name + StringUtils.EOL);
            }
            
            if (!StringUtils.isNullOrEmpty(description))
            {
                infoBuilder.append(description + StringUtils.EOL);
            }
            
            if (!StringUtils.isNullOrEmpty(category))
            {
                infoBuilder.append(Default.SPACE_INFO.format(category));
            }
            
            return infoBuilder.toString();
        }
    }

    private Floor floor;

    public ImageMapWriter(Floor floor, GlobalSettings settings)
    {
        super(settings);
        this.floor = floor;
    }

    boolean entitySettingInFloor(EntitySetting entitySetting)
    {
        boolean rval = false;

        for (RenderableIfcProduct product : getFloor().getTree())
        {
            if (entitySetting.getProductType().isAssignableFrom(product.getProductType()))
            {
                rval = true;
                break;
            }
        }

        return rval;
    }

    public final Floor getFloor()
    {
        return floor;
    }

    @Override
    public void write() throws IOException
    {
        write(null);
    }

    @Override
    public void write(Zipper zipper) throws IOException
    {
        writeImageMap(zipper);
        writeImageMapKey(zipper);
    }

    private void writeImageMap(Zipper zipper) throws IOException
    {
    	FileInfo htmlInfo = getSettings().getOutputSettings().getHtmlInfo();
        File mapFile = new File(getFloor().getFileName(htmlInfo.getExtension()));
        writeImageMapHtml(mapFile);

        if (zipper != null)
        {
            zipper.addEntry(mapFile, htmlInfo.path + mapFile.getPath(), true);
        }
    }

    private void writeImageMapHtml(File mapFile) throws IOException
    {
    	FileInfo imageInfo = getSettings().getOutputSettings().getImageInfo();
        FileWriter fileWriter = new FileWriter(mapFile);

        String imageFileName = 
                OutputSettings.PARENT_DIRECTORY + 
                imageInfo.path + 
                getFloor().getFileName(imageInfo.getExtension());

        String template = getTemplate(Resource.FLOORMAP);
        template = template.replace(Pattern.FLOOR_MAP_IMAGE.toString(), imageFileName);
        template = template.replace(Pattern.FLOOR_NAME.toString(), getFloor().getName());

        String areas = Common.EMPTY_STRING.toString();
        int spaceNum = 0;

        for (RenderableIfcProduct product : getFloor().getTree())
        {
            if (product instanceof Space)
            {
                spaceNum++;
                
                SpaceWriter spaceWriter = new SpaceWriter((Space)product, Integer.toString(spaceNum), getTemplate(Resource.AREA));
                areas += spaceWriter.write();
            }
        }

        template = template.replace(Pattern.AREAS.toString(), areas);

        fileWriter.write(template);
        fileWriter.close();
    }

    private void writeImageMapKey(Zipper zipper) throws IOException
    {
    	FileInfo htmlInfo = getSettings().getOutputSettings().getHtmlInfo();
        String mapKeyFileName = getFloor().getFileName(htmlInfo.getExtension());
        
        mapKeyFileName = mapKeyFileName.replace(
        					Common.FILE_EXTENSION_PREFIX.toString(), 
        					Default.DEFAULT_KEY_POSTFIX.toString() + Common.FILE_EXTENSION_PREFIX.toString());

        File mapKeyFile = new File(mapKeyFileName);
        writeImageMapKeyHtml(mapKeyFile);

        if (zipper != null)
        {
            zipper.addEntry(mapKeyFile, htmlInfo.path + mapKeyFile.getPath(), true);
        }
    }

    private void writeImageMapKeyHtml(File mapKeyFile) throws IOException
    {
        FileWriter fileWriter = new FileWriter(mapKeyFile);
        String template = getTemplate(Resource.KEY);
        String keys = Common.EMPTY_STRING.toString();

        for (EntitySetting entitySetting : getSettings().getRenderSettings().getEntitySettings())
        {
            if (entitySettingInFloor(entitySetting))
            {
                String keyTemplate = getTemplate(Resource.KEY_INFO);

                COBieColor color = COBieColor.cast(entitySetting.getFillColor());
                keyTemplate = keyTemplate.replace(Pattern.COLOR.toString(), color.toHexString());

                String productName = getSettings().
                                        getRenderSettings().
                                        getEntitySetting(entitySetting.getProductType()).
                                        getFriendlyName();
                
                keyTemplate = keyTemplate.replace(Pattern.NAME.toString(), productName);

                // Don't want all this jibber-jabber.
                keyTemplate = keyTemplate.replace(Default.PACKAGE.toString(), Common.EMPTY_STRING.toString());

                keys += keyTemplate;
            }
        }

        template = template.replace(Pattern.KEY_INFOS.toString(), keys);

        fileWriter.write(template);
        fileWriter.close();
    }
}
