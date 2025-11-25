{
  description = "A very basic flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
  };

  outputs = {
    self,
    nixpkgs,
  }: let
    system = "x86_64-linux";
    pkgs = nixpkgs.legacyPackages.${system};
  in {
    devShells.${system} = rec {

			default = back;

      back = pkgs.mkShell {
        packages = with pkgs; [
          jdk21
          maven
        ];
        shellHook = ''
          export SHELL="/run/current-system/sw/bin/bash"
        '';
      };
    };
  };
}
