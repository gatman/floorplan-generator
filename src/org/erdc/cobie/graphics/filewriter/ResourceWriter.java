package org.erdc.cobie.graphics.filewriter;

import java.io.File;
import java.io.IOException;

import org.erdc.cobie.graphics.settings.GlobalSettings;
import org.erdc.cobie.shared.Zipper;

public class ResourceWriter extends FileWriter
{
    public ResourceWriter(GlobalSettings settings)
    {
        super(settings);
    }

    @Override
    public void write() throws IOException
    {
        write(null);       
    }

    @Override
    public void write(Zipper zipper) throws IOException
    {
        for(String key : getSettings().getOutputSettings().getResources().keySet())
        {
            File file = getSettings().getOutputSettings().getResources().get(key);            
            zipper.addEntry(file, key, false);
        }        
    }
}
