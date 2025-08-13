package modelo;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import archivosSoloLectura.datosSonidoLectura;

public class XMLManager {
	
	public static void saveXML(List<datosSonidoLectura> datosGuardar,String ruta)
	{
		try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("sonidos");
            doc.appendChild(rootElement);

            for (datosSonidoLectura datos : datosGuardar) {
                Element sonido = doc.createElement("sonido");

                Element nombre = doc.createElement("nombre");
                nombre.setTextContent(datos.getNombreArchivo());

                Element rutaArchivo = doc.createElement("rutaArchivo");
                rutaArchivo.setTextContent(datos.getRutaArchivoAudio());
                
                Element elementoRutaImagen = doc.createElement("rutaImagen");
                
                String rutaImagen = datos.getRutaImagen();
                
                if (rutaImagen != null)
                	elementoRutaImagen.setTextContent(rutaImagen);
                 else 
                	elementoRutaImagen.setTextContent("");
                
                Element tags = doc.createElement("tags");
                List<String> listaTags = datos.getTags();
                
                String tagsComoTexto = String.join(",", listaTags);
                tags.setTextContent(tagsComoTexto);

                Element volumen = doc.createElement("volumen");
                volumen.setTextContent(String.valueOf(datos.getVolumen()));
                
                Element duracion = doc.createElement("duracion");
                duracion.setTextContent(String.valueOf(datos.getDuracion()));
                
                Element loop = doc.createElement("loop");
                loop.setTextContent(String.valueOf(datos.getLoop()));
                
                Element fadeIn = doc.createElement("fadeIn");
                fadeIn.setTextContent(String.valueOf(datos.getFadeIn()));
                
                Element fadeOut = doc.createElement("fadeOut");
                fadeOut.setTextContent(String.valueOf(datos.getFadeOut()));
                
                sonido.appendChild(nombre);
                sonido.appendChild(rutaArchivo);
                sonido.appendChild(elementoRutaImagen);
                sonido.appendChild(tags);
                sonido.appendChild(volumen);
                sonido.appendChild(duracion);
                sonido.appendChild(loop);
                sonido.appendChild(fadeIn);
                sonido.appendChild(fadeOut);

                rootElement.appendChild(sonido);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(ruta));

            transformer.transform(source, result);

            System.out.println("Archivo XML guardado en: " + ruta);

        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static List<datosSonidoLectura> loadXML(String ruta) {
        List<datosSonidoLectura> datosCargados = new ArrayList<>();

        try {
            File archivo = new File(ruta);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(archivo);
            doc.getDocumentElement().normalize();

            NodeList listaSonidos = doc.getElementsByTagName("sonido");

            for (int i = 0; i < listaSonidos.getLength(); i++) {
                Element elemSonido = (Element) listaSonidos.item(i);

                String nombre = elemSonido.getElementsByTagName("nombre").item(0).getTextContent();
                String rutaArchivo = elemSonido.getElementsByTagName("rutaArchivo").item(0).getTextContent();
                String rutaImagen = elemSonido.getElementsByTagName("rutaImagen").item(0).getTextContent();
                float volumen = Float.parseFloat(elemSonido.getElementsByTagName("volumen").item(0).getTextContent());
                float duracion = Float.parseFloat(elemSonido.getElementsByTagName("duracion").item(0).getTextContent());
                boolean loop = Boolean.parseBoolean(elemSonido.getElementsByTagName("loop").item(0).getTextContent());
                float fadeIn = Float.parseFloat(elemSonido.getElementsByTagName("fadeIn").item(0).getTextContent());
                float fadeOut = Float.parseFloat(elemSonido.getElementsByTagName("fadeOut").item(0).getTextContent());
                String tagsConcatenados = elemSonido.getElementsByTagName("tags").item(0).getTextContent();
                List<String> listaTags;
                
                if (tagsConcatenados == null || tagsConcatenados.isEmpty()) {
                     listaTags = List.of(); // lista vacía inmutable
                }
                else
                	listaTags = List.of(tagsConcatenados.split(","));
            	
                datosSonidoLectura datos = new datosSonidoLectura(rutaArchivo,nombre,i,
                													volumen,duracion,fadeIn,fadeOut,loop);
                datos.setRutaImagen(rutaImagen);
                datos.setListaTags(listaTags);
                datosCargados.add(datos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return datosCargados;
    }
	
	public static List<datosSonidoLectura> loadSoftRope(String ruta, String rutaImagenes) {
	    List<datosSonidoLectura> datosCargados = new ArrayList<>();

	    try {
	        File archivoXML = new File(ruta);
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = dBuilder.parse(archivoXML);
	        doc.getDocumentElement().normalize();

	        NodeList scenes = doc.getElementsByTagName("Scene");
	        for (int i = 0; i < scenes.getLength(); i++) {
	            Element scene = (Element) scenes.item(i);

	            // SoundEffect
	            Element soundEffect = (Element) scene.getElementsByTagName("SoundEffect").item(0);
	            boolean isLooping = Boolean.parseBoolean(getTagValue("IsLooping", soundEffect));

	            // Nombre directo hijo <Name> de Scene (no buscar recursivamente)
	            String nombreBoton = getDirectChildValue("Name", scene);

	            // Sample (siempre uno)
	            Element sample = (Element) soundEffect.getElementsByTagName("Sample").item(0);
	            String fileName = getTagValue("FileName", sample);
	            Double volumen = Double.parseDouble(getTagValue("Volume", sample));

	            // Imagen (opcional)
	            String imagenBase64 = getTagValue("ButtonImage", scene);
	            String rutaImagen = "";
	            if (!rutaImagenes.isEmpty() && imagenBase64 != null && !imagenBase64.isBlank()) {
	                rutaImagen =  guardarImagenBase64(imagenBase64, rutaImagenes, nombreBoton + ".png");
	            }

	            System.out.println(rutaImagen);

	            // Crear objeto datosSonidoLectura (ajustá constructor y parámetros si es necesario)
	            datosSonidoLectura datos = new datosSonidoLectura(fileName, nombreBoton, 0, volumen, 0, 0, 0, isLooping);
	            
	            if(rutaImagen != "")
	            	datos.setRutaImagen(rutaImagen);
	            
	            datosCargados.add(datos);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return datosCargados;
	}

	// Función para obtener solo hijos directos con cierto tag (no busca recursivamente)
	private static String getDirectChildValue(String tag, Element parent) {
	    NodeList children = parent.getChildNodes();
	    for (int i = 0; i < children.getLength(); i++) {
	        Node node = children.item(i);
	        if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(tag)) {
	            return node.getTextContent();
	        }
	    }
	    return "";
	}

	// Función que ya tenías para obtener tag recursivo
	private static String getTagValue(String tag, Element element) {
	    if (element == null) return "";
	    NodeList nodeList = element.getElementsByTagName(tag);
	    if (nodeList != null && nodeList.getLength() > 0) {
	        Node node = nodeList.item(0);
	        return node.getTextContent();
	    }
	    return "";
	}

	// Guardar imagen base64 a archivo
	private static String guardarImagenBase64(String base64, String carpetaDestino, String nombreArchivo) {
	    try {
	        byte[] bytes = Base64.getDecoder().decode(base64);
	        Path path = Paths.get(carpetaDestino, nombreArchivo);
	        Files.write(path, bytes);
	        return path.toUri().toString();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    return "";
	}

}
