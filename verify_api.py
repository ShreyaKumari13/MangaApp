import requests
import json
import time

# API configuration
url = "https://mangaverse-api.p.rapidapi.com/manga/fetch"

# Headers - exactly as shown in your Postman screenshot
headers = {
    "X-RapidAPI-Key": "14a88debeamsh00fec5566b32637p11d1e0jsn52bd5dc0db14",
    "X-RapidAPI-Host": "mangaverse-api.p.rapidapi.com"
}

print("Making request to MangaVerse API...")
print(f"URL: {url}")
print(f"Headers: {headers}")

try:
    # Make the request with no query parameters (just like in your screenshot)
    start_time = time.time()
    response = requests.get(url, headers=headers)
    elapsed_time = time.time() - start_time
    
    # Print response details
    print(f"\nStatus Code: {response.status_code}")
    print(f"Response Time: {elapsed_time:.2f} seconds")
    
    if response.status_code == 200:
        data = response.json()
        print("\nAPI Response Summary:")
        
        # Check the structure of the response
        print(f"Response Keys: {list(data.keys())}")
        
        if 'data' in data:
            items = data.get('data', [])
            print(f"Total Manga Items: {len(items)}")
            
            if items and len(items) > 0:
                print("\nFirst Manga Item:")
                first_item = items[0]
                print(f"Keys in first item: {list(first_item.keys())}")
                print(f"ID: {first_item.get('id', 'Unknown')}")
                print(f"Title: {first_item.get('title', 'Unknown')}")
                print(f"Genres: {first_item.get('genres', [])}")
                print(f"Status: {first_item.get('status', 'Unknown')}")
                
        # Save the full response to a file for inspection
        with open("verify_api_response.json", "w") as f:
            json.dump(data, f, indent=2)
            print("\nFull response saved to verify_api_response.json")
    else:
        print(f"Error: {response.text}")
        
except Exception as e:
    print(f"Exception occurred: {str(e)}")
