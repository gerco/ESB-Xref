package nl.progaia.esbxref;

import com.sonicsw.deploy.*;
import com.sonicsw.deploy.artifact.ArtifactFactory;
import com.sonicsw.deploy.storage.AbstractArtifactStorage;
import java.util.ArrayList;

public class TraversalContext
    implements IArtifactTraversalContext
{
    private ArrayList<IArtifact> m_ignored;
    private ArrayList<IArtifact> m_traversed;
    private ArrayList<IArtifact> m_errored;
    
    private IArtifactStorage m_store;
    private boolean m_traverseCompressed;

    public TraversalContext(IArtifactStorage iartifactstorage, IArtifact aiartifact[])
    {
        m_ignored = new ArrayList<IArtifact>();
        m_traversed = new ArrayList<IArtifact>();
        m_errored = new ArrayList<IArtifact>();
        m_store = iartifactstorage;
        if(aiartifact != null)
        {
            for(int i = 0; i < aiartifact.length; i++)
                m_ignored.add(aiartifact[i]);

        }
    }

    public boolean isIgnored(IArtifact iartifact)
    {
        IArtifact iartifact1 = iartifact;
        do
        {
            if(m_ignored.contains(iartifact1))
                return true;
            if(!iartifact1.isRootPath())
                iartifact1 = ArtifactFactory.createArtifact(iartifact1.getDisplayType(), iartifact1.getParentPath());
            else
                return false;
        } while(true);
    }

    public IArtifactStorage getStorage()
    {
        return m_store;
    }

    public boolean addTraversed(IArtifact a)
    {
//    	System.out.println(a.getPath());
        return addTraversed(a, true);
    }

    public boolean addTraversed(IArtifact iartifact, boolean flag)
    {
        boolean flag1 = false;
        if(isTraversed(iartifact))
            ((AbstractArtifactStorage)m_store)._notifyMessage(3, "Skipping '" + iartifact + "' - artifact has already been handled");
        else
        if(isIgnored(iartifact) && flag)
        {
            ((AbstractArtifactStorage)m_store)._notifyMessage(3, "Skipping '" + iartifact + "' - it is on the ignore list");
        } else
        {
            m_traversed.add(iartifact);
            flag1 = true;
        }
        return flag1;
    }

    public boolean isTraversed(IArtifact iartifact)
    {
        return m_traversed.contains(iartifact);
    }

    public IArtifact[] getTraversed()
    {
        return (IArtifact[])m_traversed.toArray(new IArtifact[0]);
    }

    public IArtifact[] getIgnored()
    {
        return (IArtifact[])m_ignored.toArray(new IArtifact[0]);
    }

    public void addErrored(IArtifact iartifact)
    {
        if(!m_errored.contains(iartifact))
            m_errored.add(iartifact);
    }

    public IArtifact[] getErrored()
    {
        return (IArtifact[])m_errored.toArray(new IArtifact[0]);
    }

    public IArtifact[] completeTraversal()
    {
        ArrayList<IArtifact> result = new ArrayList<IArtifact>();
        for(int i = 0; i < m_traversed.size(); i++)
        {
            IArtifact iartifact = (IArtifact)m_traversed.get(i);
            if(!iartifact.isDirectory() && !iartifact.isRootPath())
                result.add(iartifact);
        }

        for(int j = 0; j < m_errored.size(); j++)
        {
            IArtifact iartifact1 = (IArtifact)m_errored.get(j);
            ((AbstractArtifactStorage)m_store)._notifyErrorMessage("Removing '" + iartifact1 + "' from the traversal results, there was an error during traversal.", null);
            result.remove(iartifact1);
        }

        return (IArtifact[])result.toArray(new IArtifact[0]);
    }

    public boolean traverseCompressed()
    {
        return m_traverseCompressed;
    }

    public void setTraverseCompressed(boolean flag)
    {
        m_traverseCompressed = flag;
    }
}

