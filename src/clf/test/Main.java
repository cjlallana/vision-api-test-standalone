package clf.test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Scanner;
//import java.util.logging.Logger;

import clf.test.VisionUtils;

import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

public class Main {

	//private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	private static final String WORKING_DIR = System.getProperty("user.dir");

	private static Scanner sc;
	
	private static Image image_file; // base64 encoded image
	private static Vision vision_service;
	private static ArrayList<Feature> features_list;

	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Put the images you want to analyze inside: "
				+ WORKING_DIR + "/images");
		
		System.out.println("\nSelect the image you want to analyze: ");
		
		sc = new Scanner(System.in);
		
		// Let the user select an image from the working directory
		try {
		image_file = VisionUtils.selectImage(sc, WORKING_DIR);
		} catch (IOException e) {
			System.out.println("Error selecting the image: " + e.getMessage());
			return;
		}

		System.out.println("\nSelect the feature you want to run: ");
		System.out.println("\n[1] Face detection" + 
				"\n[2] Landmark detection" +
				"\n[3] Logo detection" +
				"\n[4] Label detection" +
				"\n[5] Text detection" +
				"\n[6] Safe search detection" +
				"\n[7] Image properties" + ""
				//"\n[8] Type unspecified"
				);
		
		// Adds the selected feature to the list
		features_list = new ArrayList<Feature>();
		features_list.add(VisionUtils.selectFeature(sc.nextInt()));
		
		// Build the Vision API client service.
		try {
			vision_service = VisionUtils.getVisionService();
		} catch (GeneralSecurityException | IOException e) {
			System.out.println("Error getting the Vision service: "
					+ e.getMessage());
			return;			
		}
		
		// Send the request and receive the response from Vision API
		BatchAnnotateImagesResponse response = VisionUtils
				.sendProcessImageRequest(vision_service, image_file, features_list);
		System.out.println(VisionUtils.convertResponseToString(response));

		sc.close();
	}
}
