package clf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

public class VisionUtils {

	/**
	 * Build and return an authorized Vision API client service.
	 * 
	 * @return an authorized Vision API client service
	 * @throws GeneralSecurityException
	 */
	public static Vision getVisionService() throws GeneralSecurityException,
			IOException {
		HttpTransport httpTransport = GoogleNetHttpTransport
				.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		// Build service account credential.
		GoogleCredential credential = GoogleCredential.fromStream(
				new FileInputStream(
						"credentials/vision-api-service-account.json"))
				.createScoped(VisionScopes.all());
		// Set up global Plus instance.
		return new Vision.Builder(httpTransport, jsonFactory, credential)
				.setApplicationName("VisionApiTest").build();
	}

	/**
	 * Prepares and sends the request for performing Google Cloud Vision API
	 * tasks over a user-provided image, with user- requested features.
	 * 
	 * @param vision_service an authorized Vision API client service
	 * @param image_file base64 encoded image
	 * @param features_list list of requested Vision API features.
	 * @throws IOException
	 */
	@SuppressWarnings("serial")
	public static BatchAnnotateImagesResponse sendProcessImageRequest(
			Vision vision_service, 
			final Image image_file, 
			final ArrayList<Feature> features_list) 
					throws IOException {
		
		BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
		batchAnnotateImagesRequest
				.setRequests(new ArrayList<AnnotateImageRequest>() {
					{
						AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
						annotateImageRequest.setImage(image_file);
						annotateImageRequest.setFeatures(features_list);
						add(annotateImageRequest);
					}
				});

		Vision.Images.Annotate annotateRequest = vision_service.images()
				.annotate(batchAnnotateImagesRequest);
		// Due to a bug: requests to Vision API containing large images fail
		// when GZipped.
		annotateRequest.setDisableGZipContent(true);

		return annotateRequest.execute();
	}

	/**
	 * Returns the selected Feature.
	 * 
	 * @param option integer
	 * @return Feature already set
	 */
	public static Feature selectFeature(int option) {
		Feature feature = new Feature();
		String type = null;
		int max_results = 10;

		switch (option) {
		case 1:
			type = "FACE_DETECTION";
			break;
		case 2:
			type = "LANDMARK_DETECTION";
			break;
		case 3:
			type = "LOGO_DETECTION";
			break;
		case 4:
			type = "LABEL_DETECTION";
			break;
		case 5:
			type = "TEXT_DETECTION";
			break;
		case 6:
			type = "SAFE_SEARCH_DETECTION";
			break;
		case 7:
			type = "IMAGE_PROPERTIES";
			break;
		case 8:
			type = "TYPE_UNSPECIFIED";
			break;
		}

		feature.setType(type);
		feature.setMaxResults(max_results);
		return feature;
	}

	/**
	 * Lets the user select an image from the file system, and returns the image
	 * encoded in base64.
	 * 
	 * @param working_dir
	 *            points to the working directory of the project.
	 * @return base64 encoded image
	 * @throws IOException
	 */
	public static Image selectImage(Scanner sc, String working_dir) 
			throws IOException 
	{
		File folder = new File(working_dir + "/images");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile())
				System.out.println("[" + (i + 1) + "] "
						+ listOfFiles[i].getName());
		}

		File image_file = listOfFiles[sc.nextInt() - 1];
		
		byte[] imageBytes = Files.readAllBytes(image_file.toPath());

		// Base64 encode the JPEG
		return new Image().encodeContent(imageBytes);
	}
	

	/**
	 * Formats the Vision API response to a readable version.
	 * 
	 * @param response raw AnnotateImage response
	 * @return readable response
	 */
	public static String convertResponseToString(
			BatchAnnotateImagesResponse response) {
		String message = "I found these things:\n\n";

		List<EntityAnnotation> labels = response.getResponses().get(0)
				.getLabelAnnotations();
		if (labels != null) {
			for (EntityAnnotation label : labels) {
				message += String.format("%.3f: %s", label.getScore(),
						label.getDescription());
				message += "\n";
			}
		} else {
			message += "nothing";
		}

		return message;
	}
}
