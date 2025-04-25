import subprocess
import json

# Construct the exact cURL command from the screenshots
curl_command = [
    'curl', '--request', 'GET',
    '--url', 'https://mangaverse-api.p.rapidapi.com/manga/fetch?page=1&genres=Harem%2CFantasy&nsfw=true&type=all',
    '--header', 'x-rapidapi-host: mangaverse-api.p.rapidapi.com',
    '--header', 'x-rapidapi-key: 14a88debeamsh00fec5566b32637p1b1e0jsn52bd5dc0db14'
]

print("Executing cURL command:")
print(" ".join(curl_command))
print("\n")

# Execute the command
try:
    result = subprocess.run(curl_command, capture_output=True, text=True)
    
    # Print the status code (if available)
    print(f"Status Code: {result.returncode}")
    
    # Print the output
    print("\nResponse:")
    print(result.stdout)
    
    # Check if the response is valid JSON
    try:
        response_data = json.loads(result.stdout)
        print("\nSuccessfully parsed JSON response")
        
        # Save the response to a file
        with open("manga_curl_response.json", "w") as f:
            json.dump(response_data, f, indent=2)
            print("Full response saved to manga_curl_response.json")
            
    except json.JSONDecodeError:
        print("Response is not valid JSON")
        
    # Print any errors
    if result.stderr:
        print("\nErrors:")
        print(result.stderr)
        
except Exception as e:
    print(f"Error executing cURL command: {str(e)}")
