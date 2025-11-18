package com.example;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

public class LabExampleVolume {

  private static Clip audioClip;

  private static String directoryPath = "F:\\Song For Spotify App\\Song";

  public static void main(final String[] args) {

    Song[] library = readAudioLibrary();

    Scanner input = new Scanner(System.in);

    String userInput = "";
    while (!userInput.equals("q")) {
      menu(library);
      userInput = handleMenu(input, library);
    }

    input.close();
  }

  public static float getVolume() {
    FloatControl gainControl = (FloatControl) audioClip.getControl(
        FloatControl.Type.MASTER_GAIN);
    return (float) Math.pow(10f, gainControl.getValue() / 20f);
  }

  public static void setVolume(float volume) {
    if (volume < 0f || volume > 1f)
      throw new IllegalArgumentException(
          "Volume not valid: " + volume);
    FloatControl gainControl = (FloatControl) audioClip.getControl(
        FloatControl.Type.MASTER_GAIN);
    gainControl.setValue(20f * (float) Math.log10(volume));
  }

  public static void adjustVolume(Scanner input) {
    if (audioClip == null) {
      System.out.println("No audio is currently playing.");
      return;
    }

    System.out.print("Enter volume (0 - 100): ");
    String line = input.nextLine();

    try {
      int vol = Integer.parseInt(line);
      if (vol < 0 || vol > 100) {
        System.out.println("Volume must be between 0 and 100.");
        return;
      }
      float v = vol / 100f;
      if (v < 0.01f)
        v = 0.01f;
      setVolume(v);
      System.out.println("Volume updated to " + vol + "%");

    } catch (Exception e) {
      System.out.println("Invalid input. Please enter numbers 0–100.");
    }
  }

  public static void menu(Song[] library) {
    System.out.println("---- SpotifyLikeApp ----");

    for (Integer i = 0; i < library.length; i++) {
      String name = library[i].name();
      System.out.printf("[%d] %s\n", i + 1, name);
    }

    if (audioClip != null) {
      System.out.println("[V]olume");
    }

    System.out.println("[Q]uit");

    if (audioClip != null) {
      Integer vol = (int) Math.ceil(getVolume() * 100.0f);
      System.out.println("");
      System.out.printf("--- volume is %d%% ---\n", vol);
    }

    System.out.println("");
    System.out.print("Enter q to Quit:");
  }

  public static String handleMenu(Scanner input, Song[] library) {

    String menuChoice = input.nextLine();

    menuChoice = menuChoice.toLowerCase();

    try {
      Integer number = Integer.parseInt(menuChoice);

      if (number < library.length) {

        play(library, number - 1);
      }
    } catch (Exception e) {
      switch (menuChoice) {
        case "q":
          System.out.println("Thank you for using the app.");
          break;
        case "v":
          adjustVolume(input);
          break;
        default:
          System.out.printf("Error: %s is not a command\n", menuChoice);
          break;
      }
    }

    return menuChoice;
  }

  public static void play(Song[] library, Integer i) {

    final String filename = library[i].fileName();
    final String filePath = directoryPath + "/wav/" + filename;
    final File file = new File(filePath);

    if (audioClip != null) {
      audioClip.close();
    }

    try {
      audioClip = AudioSystem.getClip();

      final AudioInputStream in = AudioSystem.getAudioInputStream(file);

      audioClip.open(in);
      audioClip.setMicrosecondPosition(0);
      audioClip.loop(Clip.LOOP_CONTINUOUSLY);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Song[] readAudioLibrary() {

    final String jsonFileName = "audio-library.json";
    final String filePath = directoryPath + "/" + jsonFileName;

    Song[] library = null;
    try {
      System.out.println("Reading the file " + filePath);
      JsonReader reader = new JsonReader(new FileReader(filePath));
      library = new Gson().fromJson(reader, Song[].class);
    } catch (Exception e) {
      System.out.printf("ERROR: unable to read the file %s\n", filePath);
      System.out.println();
    }

    return library;
  }
}
