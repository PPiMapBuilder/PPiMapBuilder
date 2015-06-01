# ![](https://fr.gravatar.com/userimage/46678059/7c7f65f2ea5b01dfc46adac45048df6b.jpg?size=40) PPiMapBuilder

  PPiMapBuilder is a Cytoscape 3 app for protein-protein interaction network generation and protein-protein interaction prediction via the [Phylogenetic profiling](http://en.wikipedia.org/wiki/Protein-protein_interaction_prediction#Phylogenetic_profiling) method as described by:
>Echeverría PC, Bernthaler A, Dupuis P, Mayer B, Picard D (2011) An interaction network predicted from public data as a discovery tool: application to the Hsp90 molecular chaperone machine. [PLoS One 6: e26044.doi:10.1371/journal.pone.0026044.](http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0026044)

  PPiMapBuilder uses PSICQUIC services to retrieve interaction data, InParanoid 8 for protein orthology and UniProt for protein data.
  
  This project was initiated by Pablo Echeverría from <a href="http://www.picard.ch/"><img src="http://www.picard.ch/Hsp90Int/img/logo.png" alt="picardLab" width="93" height="20" align="texttop"/></a> as a bio-informatic master student project.
  
  To get more details about this Cytoscape 3 app, you can check the [wiki](https://github.com/PPiMapBuilder/PPiMapBuilder/wiki).

  ![PPiMapBuilder ScreenShot](https://raw.githubusercontent.com/wiki/PPiMapBuilder/PPiMapBuilder/images/Screenshot.png)

## Installation
### From release
1. Download the [lastest PPiMapBuilder release](https://github.com/PPiMapBuilder/PPiMapBuilder/releases)
2. Install `PPiMapBuilder-X.X.jar`
  * Within Cytoscape:
    - Go to `Apps>App Manager` 
    - Click `Install from file`
    - Select the PPiMapBuilder jar file

  OR
  * Manually:
    - Move the PPiMapBuilder jar file to `<USER_DIRECTORY>/CytoscapeConfiguration/3/apps/installed/`

### From source code
1. Clone this git repository or download source
2. Run `mvn install` in the PPiMapBuilder folder
3. Install `./target/PPiMapBuilder-X.X.jar`
  * Within Cytoscape:
    - Go to `Apps>App Manager` 
    - Click `Install from file`
    - Select the PPiMapBuilder jar file

  OR
  * Manually:
    - Move the PPiMapBuilder jar file to `<USER_DIRECTORY>/CytoscapeConfiguration/3/apps/installed/`

## Reference webservices used
<center>
  <table>
    <tr>
      <td valign="middle">
        <a href="https://github.com/micommunity/psicquic">
          <img src="http://apps.cytoscape.org/media/psicquicuniversalclient/psicquic_logo.png" alt="Psicquic" width="73" height="73"/>
          PSICQUIC
        </a>
      </td>
      <td>
        <a href="http://www.uniprot.org/">
          <img src="http://www.uniprot.org/images/logo.gif" alt="UniProt" width="160" height="73"/>
        </a>
      </td>
      <td>
        <a href="http://inparanoid.sbc.su.se/">
          <img src="http://inparanoid.sbc.su.se/images/inp8_txt_logo.png" alt="Inparanoid" width="269" height="73"/>
        </a>
      </td>
      <td>
        <a href="http://www.ebi.ac.uk/QuickGO/">
          <img src="http://www.ebi.ac.uk/QuickGO/image/mb/logo.png" alt="QuickGO" width="105" height="51"/>
        </a>
      </td>
    </tr>
  </table>
</center>
