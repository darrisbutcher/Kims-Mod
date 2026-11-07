# Paints the placeholder textures for the two followers and their figurine icons.
# Run from anywhere:  powershell -File tools\generate-follower-textures.ps1
# Output goes to src\main\resources\assets\securitycams\textures\{entity,item}.
Add-Type -AssemblyName System.Drawing

$root = Split-Path -Parent $PSScriptRoot
$entityDir = Join-Path $root 'src\main\resources\assets\securitycams\textures\entity'
$itemDir = Join-Path $root 'src\main\resources\assets\securitycams\textures\item'
New-Item -ItemType Directory -Force $entityDir, $itemDir | Out-Null

$script:rng = New-Object System.Random 11

function Col([string]$hex, [double]$f = 1.0) {
	$n = [Convert]::ToInt32($hex.TrimStart('#'), 16)
	$r = [Math]::Min(255, [Math]::Max(0, [int](($n -shr 16 -band 255) * $f)))
	$g = [Math]::Min(255, [Math]::Max(0, [int](($n -shr 8 -band 255) * $f)))
	$b = [Math]::Min(255, [Math]::Max(0, [int](($n -band 255) * $f)))
	[System.Drawing.Color]::FromArgb(255, $r, $g, $b)
}

function Noisy([string]$hex, [double]$amount = 0.06) {
	Col $hex (1.0 + ($script:rng.NextDouble() * 2 - 1) * $amount)
}

# A cube's texture cross, the way Minecraft lays out addBox(): top and bottom above, four sides below.
function FaceRect($cube, [string]$face) {
	$u = $cube.u; $v = $cube.v; $w = $cube.w; $h = $cube.h; $d = $cube.d
	switch ($face) {
		'top'    { return @{ x = $u + $d;          y = $v;      w = $w; h = $d } }
		'bottom' { return @{ x = $u + $d + $w;     y = $v;      w = $w; h = $d } }
		'left'   { return @{ x = $u;               y = $v + $d; w = $d; h = $h } }
		'front'  { return @{ x = $u + $d;          y = $v + $d; w = $w; h = $h } }
		'right'  { return @{ x = $u + $d + $w;     y = $v + $d; w = $d; h = $h } }
		'back'   { return @{ x = $u + 2 * $d + $w; y = $v + $d; w = $w; h = $h } }
	}
}

function FillFace($bmp, $cube, [string]$face, [string]$hex, [double]$noise = 0.06) {
	$r = FaceRect $cube $face
	for ($i = 0; $i -lt $r.w; $i++) { for ($j = 0; $j -lt $r.h; $j++) { $bmp.SetPixel($r.x + $i, $r.y + $j, (Noisy $hex $noise)) } }
}

function FillCube($bmp, $cube, [string]$hex, [string]$topHex = '', [string]$bottomHex = '') {
	foreach ($f in 'left', 'front', 'right', 'back') { FillFace $bmp $cube $f $hex }
	FillFace $bmp $cube 'top' $(if ($topHex) { $topHex } else { $hex })
	FillFace $bmp $cube 'bottom' $(if ($bottomHex) { $bottomHex } else { $hex })
}

# Paints a flat rectangle on one face. du/dv are measured from the face's left and bottom edges.
function Dec($bmp, $cube, [string]$face, [int]$du, [int]$dv, [int]$dw, [int]$dh, [string]$hex) {
	$r = FaceRect $cube $face
	for ($i = 0; $i -lt $dw; $i++) {
		for ($j = 0; $j -lt $dh; $j++) {
			$x = $r.x + $du + $i
			$y = $r.y + $r.h - $dv - $dh + $j
			if ($x -ge $r.x -and $x -lt $r.x + $r.w -and $y -ge $r.y -and $y -lt $r.y + $r.h) { $bmp.SetPixel($x, $y, (Col $hex)) }
		}
	}
}

function Cube([int]$u, [int]$v, [int]$w, [int]$h, [int]$d) { @{ u = $u; v = $v; w = $w; h = $h; d = $d } }

function Save($bmp, [string]$path) { $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose(); "wrote $path" }

# ---- Little Villager (128 x 64) -----------------------------------------------------------------
$skin = '#c79a74'; $skinD = '#b4835d'; $robe = '#7a573a'; $navy = '#27375a'; $gold = '#e8c040'; $dark = '#3a2a1a'
$bmp = New-Object System.Drawing.Bitmap 128, 64

$c = Cube 0 0 8 14 6
FillCube $bmp $c $robe
Dec $bmp $c 'front' 1 4 6 10 $navy
Dec $bmp $c 'front' 4 10 2 2 $gold
foreach ($f in 'front', 'back') { Dec $bmp $c $f 0 6 8 1 $dark }
foreach ($f in 'left', 'right') { Dec $bmp $c $f 0 6 6 1 $dark }

$c = Cube 0 20 9 4 3
FillCube $bmp $c $robe
Dec $bmp $c 'front' 1 1 7 2 $skin
foreach ($f in 'left', 'right') { Dec $bmp $c $f 0 1 3 2 $skin }

$c = Cube 24 20 5 4 1
FillCube $bmp $c '#202a38' '#202a38' '#202a38'
Dec $bmp $c 'front' 1 1 3 2 '#4aa8ee'
Dec $bmp $c 'front' 1 3 1 1 '#e24848'

$c = Cube 28 0 10 10 10
FillCube $bmp $c $skin
Dec $bmp $c 'front' 1 6 3 2 '#f3f3f3'
Dec $bmp $c 'front' 6 6 3 2 '#f3f3f3'
Dec $bmp $c 'front' 2 6 2 2 '#4d7f3d'
Dec $bmp $c 'front' 6 6 2 2 '#4d7f3d'
Dec $bmp $c 'front' 1 8 8 1 $dark

$c = Cube 68 0 2 5 3
FillCube $bmp $c $skinD
Dec $bmp $c 'front' 0 0 2 1 '#8c6141'

$c = Cube 0 40 11 2 11
FillCube $bmp $c $navy '#33497a'
Dec $bmp $c 'front' 4 0 3 2 $gold
Dec $bmp $c 'front' 5 0 1 1 '#1b2842'

$c = Cube 44 40 11 1 3
FillCube $bmp $c '#1c2a48'
Save $bmp (Join-Path $entityDir 'little_villager.png')

# ---- Veiled chameleon: one texture per camouflage look (64 x 64) --------------------------------
$palettes = [ordered]@{
	grass = @{ base = '#5fa63a'; a = '#3fb8b0'; b = '#e8d64c'; belly = '#b4d96e'; casque = '#73bb46'; eye = '#bde07c'; tail = '#4f9532' }
	sand  = @{ base = '#d6bf7a'; a = '#b8964a'; b = '#f0e2a8'; belly = '#efe2b0'; casque = '#e0cc8a'; eye = '#f0e2a8'; tail = '#c9ae62' }
	stone = @{ base = '#8d9096'; a = '#6a6d73'; b = '#b8bbc0'; belly = '#c5c7cb'; casque = '#9a9da3'; eye = '#c9ccd0'; tail = '#7d8087' }
	snow  = @{ base = '#e4ebf0'; a = '#a9c3d6'; b = '#ffffff'; belly = '#f7fafc'; casque = '#d6e2ea'; eye = '#ffffff'; tail = '#c9d8e3' }
	dirt  = @{ base = '#9a7448'; a = '#6f5030'; b = '#c9a36b'; belly = '#c9ad82'; casque = '#a98355'; eye = '#d9c08f'; tail = '#86633c' }
	nether = @{ base = '#8f2f2a'; a = '#5c1814'; b = '#d66a4a'; belly = '#b9604f'; casque = '#a53b33'; eye = '#e08a6a'; tail = '#74211d' }
}
foreach ($name in $palettes.Keys) {
	$p = $palettes[$name]
	$bmp = New-Object System.Drawing.Bitmap 64, 64

	$c = Cube 0 0 3 5 8
	FillCube $bmp $c $p.base $p.base $p.belly
	foreach ($f in 'left', 'right') { for ($i = 0; $i -lt 4; $i++) { Dec $bmp $c $f (1 + 2 * $i) 0 1 4 $(if ($i % 2 -eq 0) { $p.a } else { $p.b }) } }

	$c = Cube 0 13 4 4 5
	FillCube $bmp $c $p.base $p.base $p.belly
	Dec $bmp $c 'front' 0 1 4 1 '#2e4a1c'
	Dec $bmp $c 'front' 0 2 1 1 '#2e4a1c'
	Dec $bmp $c 'front' 3 2 1 1 '#2e4a1c'

	$c = Cube 18 13 1 2 4
	FillCube $bmp $c $p.casque
	foreach ($f in 'left', 'right') { Dec $bmp $c $f 0 0 1 2 $p.b; Dec $bmp $c $f 2 0 1 2 $p.a }

	$c = Cube 28 13 1 2 2
	FillCube $bmp $c $p.casque
	foreach ($f in 'left', 'right') { Dec $bmp $c $f 0 0 1 2 $p.a }

	$c = Cube 34 13 1 2 2
	FillCube $bmp $c $p.eye
	foreach ($f in 'left', 'right') { Dec $bmp $c $f 1 1 1 1 '#161616' }
	Dec $bmp $c 'front' 0 1 1 1 '#161616'

	$c = Cube 0 22 1 3 2
	FillCube $bmp $c $p.base
	$c = Cube 6 22 2 1 3
	FillCube $bmp $c $p.tail

	FillCube $bmp (Cube 0 27 2 2 4) $p.tail
	FillCube $bmp (Cube 12 27 2 4 2) $p.tail
	FillCube $bmp (Cube 20 27 2 2 4) $p.tail
	FillCube $bmp (Cube 32 27 2 3 2) $p.tail
	FillCube $bmp (Cube 40 27 1 1 1) $p.casque
	Save $bmp (Join-Path $entityDir "veiled_chameleon_$name.png")
}

# ---- Figurine icons (16 x 16) -------------------------------------------------------------------
function Px($bmp, [int]$x, [int]$y, [int]$w, [int]$h, [string]$hex) {
	for ($i = 0; $i -lt $w; $i++) { for ($j = 0; $j -lt $h; $j++) { $bmp.SetPixel($x + $i, $y + $j, (Col $hex)) } }
}
function ClearTransparent($bmp) { for ($i = 0; $i -lt $bmp.Width; $i++) { for ($j = 0; $j -lt $bmp.Height; $j++) { $bmp.SetPixel($i, $j, [System.Drawing.Color]::FromArgb(0, 0, 0, 0)) } } }

$bmp = New-Object System.Drawing.Bitmap 16, 16
ClearTransparent $bmp
Px $bmp 3 13 10 2 '#8a8a8a'; Px $bmp 3 13 10 1 '#a3a3a3'
Px $bmp 5 8 6 5 $robe; Px $bmp 6 9 4 4 $navy; Px $bmp 8 10 1 1 $gold; Px $bmp 5 11 6 1 $dark
Px $bmp 4 3 8 5 $skin; Px $bmp 5 5 2 1 '#ffffff'; Px $bmp 9 5 2 1 '#ffffff'; Px $bmp 6 5 1 1 '#4d7f3d'; Px $bmp 9 5 1 1 '#4d7f3d'
Px $bmp 5 4 6 1 $dark; Px $bmp 7 5 2 3 $skinD
Px $bmp 4 1 8 2 $navy; Px $bmp 3 3 10 1 '#1c2a48'; Px $bmp 7 1 2 1 $gold
Save $bmp (Join-Path $itemDir 'little_villager_figurine.png')

$bmp = New-Object System.Drawing.Bitmap 16, 16
ClearTransparent $bmp
Px $bmp 2 13 12 2 '#8a8a8a'; Px $bmp 2 13 12 1 '#a3a3a3'
Px $bmp 4 9 7 3 '#5fa63a'; Px $bmp 5 9 1 3 '#3fb8b0'; Px $bmp 7 9 1 3 '#e8d64c'; Px $bmp 9 9 1 3 '#3fb8b0'
Px $bmp 11 8 3 3 '#5fa63a'; Px $bmp 12 8 1 1 '#bde07c'; Px $bmp 13 9 1 1 '#2e4a1c'
Px $bmp 9 5 2 3 '#73bb46'; Px $bmp 9 4 1 1 '#73bb46'; Px $bmp 10 6 1 1 '#e8d64c'
Px $bmp 5 12 1 1 '#4f9532'; Px $bmp 10 12 1 1 '#4f9532'
Px $bmp 2 10 2 2 '#4f9532'; Px $bmp 1 8 2 3 '#4f9532'; Px $bmp 2 7 3 1 '#4f9532'; Px $bmp 4 8 1 2 '#4f9532'
Save $bmp (Join-Path $itemDir 'veiled_chameleon_figurine.png')
