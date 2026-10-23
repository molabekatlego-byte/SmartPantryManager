# Smart Pantry Manager

An Android app (Java) that suggests recipes you can make **only** from the ingredients currently in your pantry.

> The Android Studio project is in the `AndroidStudioProjects/SmartPantryManager` folder of this repo.

## Features
- Add, view, edit and delete pantry ingredients (name, quantity, unit, optional expiry date)
- 18 preloaded recipes
- **Strict matching:** a recipe is suggested only if *every* ingredient is in the pantry in a sufficient quantity. Partial matches are never shown.
- Handles plurals (tomato / tomatoes) and unit conversion (g / kg, ml / l, tsp / tbsp / cup)
- Recipe detail screen with ingredients and instructions
- Settings screen (toggle expiry highlighting)
- Empty-state message when no recipe can be made
- Input validation on the add/edit form
- Data persists after the app is closed

## Screens
1. Pantry list (home)
2. Add / edit ingredient
3. Suggested recipes
4. Recipe detail
5. Settings

## Tech
- Java, Android Studio
- SQLite via `SQLiteOpenHelper` (full CRUD, persistent storage)
- RecyclerView with adapters
- Intents and a bottom navigation bar
- Minimum SDK 24

## How to run
1. Clone the repo.
2. In Android Studio, choose **File > Open** and select the `AndroidStudioProjects/SmartPantryManager` folder.
3. Wait for Gradle to sync.
4. Run the `app` configuration on an emulator (tested on Pixel 6, API 34).

## Author
Katlego, BSc IT, Richfield Graduate Institute of Technology
Mobile App Development 700
