import os
import requests
from PIL import Image
from io import BytesIO

def download_image(url, filename):
    try:
        response = requests.get(url)
        if response.status_code == 200:
            img = Image.open(BytesIO(response.content))
            img.save(filename)
            print(f"Downloaded {filename}")
        else:
            print(f"Failed to download {filename}")
    except Exception as e:
        print(f"Error downloading {filename}: {e}")

def main():
    # Create images directory if it doesn't exist
    if not os.path.exists("images"):
        os.makedirs("images")

    # Base URL for chess piece images (using a free chess piece set)
    base_url = "https://raw.githubusercontent.com/lichess-org/lila/master/public/piece/cburnett/"
    
    # List of pieces to download
    pieces = ["king", "queen", "rook", "bishop", "knight", "pawn"]
    colors = ["white", "black"]
    
    # Download each piece
    for color in colors:
        for piece in pieces:
            filename = f"images/{color}_{piece}.png"
            url = f"{base_url}{color[0]}{piece[0]}.svg"
            download_image(url, filename)

if __name__ == "__main__":
    main() 