import requests
import json
import time

# API configuration
url = "https://mangaverse-api.p.rapidapi.com/manga/fetch"

# Headers - exactly as shown in Postman
headers = {
    "X-RapidAPI-Key": "14a88debeamsh00fec5566b32637p1b1e0jsn52bd5dc0db14",
    "X-RapidAPI-Host": "mangaverse-api.p.rapidapi.com"
}

print("Making request to MangaVerse API...")
print(f"URL: {url}")
print(f"Headers: {headers}")

try:
    # Make the request with no query parameters
    start_time = time.time()
    response = requests.get(url, headers=headers)
    elapsed_time = time.time() - start_time
    
    # Print response details
    print(f"\nStatus Code: {response.status_code}")
    print(f"Response Time: {elapsed_time:.2f} seconds")
    
    if response.status_code == 200:
        try:
            data = response.json()
            print("\nAPI Response Summary:")
            
            if 'data' in data:
                items = data.get('data', [])
                print(f"Total Manga Items: {len(items)}")
                
                if items and len(items) > 0:
                    print("\nFirst 3 Manga Titles:")
                    for i, item in enumerate(items[:3]):
                        print(f"{i+1}. {item.get('title', 'Unknown Title')}")
                        print(f"   - Subtitle: {item.get('sub_title', 'N/A')}")
                        print(f"   - Status: {item.get('status', 'N/A')}")
                        print(f"   - Thumbnail: {item.get('thumb', 'N/A')[:100]}...")
                        
                        if 'summary' in item:
                            summary = item.get('summary', '')
                            print(f"   - Summary: {summary[:100]}..." if len(summary) > 100 else f"   - Summary: {summary}")
                        
                        if 'authors' in item:
                            authors = item.get('authors', [])
                            print(f"   - Authors: {', '.join(authors)}")
                        
                        if 'genres' in item:
                            genres = item.get('genres', [])
                            print(f"   - Genres: {', '.join(genres)}")
                        
                        print()
            
            # Save the full response to a file
            with open("api_response.json", "w") as f:
                json.dump(data, f, indent=2)
                print("Full response saved to api_response.json")
        except json.JSONDecodeError:
            print(f"Error: Response is not valid JSON: {response.text[:200]}...")
    else:
        print(f"Error: {response.text}")
        
except Exception as e:
    print(f"Exception occurred: {str(e)}")
