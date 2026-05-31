import SwiftUI

struct ContentView: View {
    @State private var isShowingDocumentPicker = false
    @State private var importedSongs: [URL] = []
    @StateObject private var audioPlayer = AudioPlayerManager.shared

    var body: some View {
        NavigationView {
            VStack {
                if importedSongs.isEmpty {
                    VStack {
                        Image(systemName: "music.note.list")
                            .font(.system(size: 60))
                            .foregroundColor(.accentColor)
                            .padding(.bottom, 10)
                        Text("Welcome to Yinyue")
                            .font(.title)
                            .bold()
                        Text("Tap the + button to import local music files.")
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding()
                    }
                } else {
                    List(importedSongs, id: \.self) { songURL in
                        Button(action: {
                            audioPlayer.play(url: songURL)
                        }) {
                            HStack {
                                Image(systemName: "music.note")
                                    .foregroundColor(.accentColor)
                                Text(songURL.lastPathComponent)
                                    .foregroundColor(.primary)
                                Spacer()
                                if audioPlayer.currentSongTitle == songURL.lastPathComponent && audioPlayer.isPlaying {
                                    Image(systemName: "waveform")
                                        .foregroundColor(.accentColor)
                                }
                            }
                        }
                    }
                }
                
                if !audioPlayer.currentSongTitle.isEmpty && audioPlayer.currentSongTitle != "Not Playing" {
                    // Now Playing Bar
                    HStack {
                        VStack(alignment: .leading) {
                            Text(audioPlayer.currentSongTitle)
                                .font(.headline)
                                .lineLimit(1)
                            Text("Now Playing")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Button(action: {
                            audioPlayer.togglePlayPause()
                        }) {
                            Image(systemName: audioPlayer.isPlaying ? "pause.circle.fill" : "play.circle.fill")
                                .font(.system(size: 40))
                        }
                    }
                    .padding()
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(12)
                    .padding(.horizontal)
                    .padding(.bottom, 10)
                }
            }
            .navigationTitle("Library")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        isShowingDocumentPicker = true
                    }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $isShowingDocumentPicker) {
                DocumentPicker(isPresented: $isShowingDocumentPicker) { urls in
                    // Add imported URLs
                    for url in urls {
                        if !importedSongs.contains(url) {
                            importedSongs.append(url)
                        }
                    }
                }
            }
        }
    }
}
